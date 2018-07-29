package forex.services.oneforge.interpreters

import akka.NotUsed
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.Uri.{ Path, Query }
import akka.http.scaladsl.model.{ HttpRequest, Uri }
import akka.stream.scaladsl.{ Flow, Sink, Source }
import akka.util.ByteString
import com.typesafe.scalalogging.StrictLogging
import forex.config.OneForgeConfig
import forex.domain.Rate
import forex.main.{ ActorSystems, Executors }
import forex.services.oneforge.{ Algebra, Error }
import monix.eval.Task
import org.atnos.eff.Eff
import org.atnos.eff.addon.monix.TaskCreation
import org.atnos.eff.addon.monix.task._task

import scala.concurrent.ExecutionContext

final class OneForgeClient[R](
    config: OneForgeConfig,
    actorSystems: ActorSystems,
    executors: Executors
)(
    implicit
    m1: _task[R]
) extends Algebra[Eff[R, ?]]
    with TaskCreation
    with StrictLogging {
  import actorSystems._

  private[this] implicit val ec: ExecutionContext = executors.default

  private[this] val streamingRequestFlow: Flow[HttpRequest, ByteString, NotUsed] =
    Flow[HttpRequest]
      .flatMapConcat(
        req ⇒
          Source
            .fromFuture(Http().singleRequest(req))
            .mapAsync(1)(_.entity.toStrict(config.readTimeout).map(_.data))
      )

  override def getAll(pairs: Seq[Rate.Pair]): Eff[R, Error Either Seq[Rate]] =
    fromTask(
      Task.fromFuture(
        Source
          .single(HttpRequest(uri = quotesRequest(pairs)))
          .via(streamingRequestFlow)
          .runWith(Sink.head)
      )
    ).map(r ⇒ Parser.parse(r.utf8String))

  private[this] def quotesRequest(pairs: Seq[Rate.Pair]): Uri =
    Uri(config.baseUri)
      .withPath(Path("/1.0.3/quotes"))
      .withQuery(Query(("pairs", pairs.map(concat).mkString(",")), ("api_key", config.apiKey)))

  private[this] def concat(pair: Rate.Pair): String = s"${pair.from.entryName}${pair.to.entryName}"
}
