package forex.services.oneforge

import java.time.Instant

import akka.NotUsed
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.Uri.{Path, Query}
import akka.http.scaladsl.model.{HttpRequest, Uri}
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.util.ByteString
import forex.config.{CacheConfig, OneForgeConfig}
import forex.domain._
import forex.main.{ActorSystems, Executors}
import io.circe.{Decoder, DecodingFailure, HCursor}
import monix.eval.{MVar, Task}
import org.atnos.eff._
import org.atnos.eff.addon.monix.TaskCreation
import org.atnos.eff.addon.monix.task._

import scala.concurrent.ExecutionContext

object Interpreters {
  def dummy[R](implicit m1: _task[R]): Algebra[Eff[R, ?]] = new Dummy[R]

  def real[R](
      oneForgeConfig: OneForgeConfig,
      executors: Executors,
      actorSystems: ActorSystems
  )(
      implicit
      m1: _task[R]
  ): Algebra[Eff[R, ?]] =
    new OneForgeClient[R](oneForgeConfig, actorSystems, executors)

  def cached[R](
      cacheConfig: CacheConfig,
      delegate: Algebra[Eff[R, ?]]
  )(
      implicit
      m1: _task[R]
  ): Algebra[Eff[R, ?]] = new Cached[R](delegate, cacheConfig)
}

final class Dummy[R] private[oneforge] (implicit m1: _task[R]) extends Algebra[Eff[R, ?]] {
  override def getAll(pairs: Seq[Rate.Pair]): Eff[R, Error Either Seq[Rate]] =
    for {
      result ← fromTask(Task.now(pairs.map(dummyRate)))
    } yield Right(result)

  private[this] def dummyRate(pair: Rate.Pair) = Rate(pair, Price(BigDecimal(100)), Timestamp.now)
}

final class OneForgeClient[R](
    config: OneForgeConfig,
    actorSystems: ActorSystems,
    executors: Executors
)(
    implicit
    m1: _task[R]
) extends Algebra[Eff[R, ?]]
    with TaskCreation {
  import actorSystems._

  private[this] implicit val ec: ExecutionContext = executors.default
  private[this] implicit val decoder: Decoder[Rate] = (c: HCursor) ⇒
    for {
      pair ← c.downField("symbol").as[String].flatMap { s ⇒
        (for {
          symbol <- if (s.length == 6) Some(s) else None
          from ← Currency.withNameOption(symbol.substring(0, 3))
          to ← Currency.withNameOption(symbol.substring(3, 6))
        } yield Rate.Pair(from, to)).toRight(DecodingFailure(s"Symbol '$s' is invalid!", List()))
      }
      price ← c.downField("price").as[BigDecimal]
      ts ← c.downField("timestamp").as[Long]
    } yield Rate(pair, Price(price), Timestamp.ofEpochMilli(ts))

  private[this] val streamingRequestFlow: Flow[HttpRequest, ByteString, NotUsed] =
    Flow[HttpRequest]
      .flatMapConcat(
        req ⇒
          Source
            .fromFuture(Http().singleRequest(req))
            .flatMapConcat(_.entity.dataBytes)
      )

  override def getAll(pairs: Seq[Rate.Pair]): Eff[R, Error Either Seq[Rate]] =
    for {
      response ← fromTask(
        Task.fromFuture(
          Source
            .single(HttpRequest(uri = quotesRequest(pairs)))
            .via(streamingRequestFlow)
            .runWith(Sink.head)
        )
      )
    } yield {
      import io.circe.jawn._
      val seq = parse(response.utf8String).map(s ⇒ Decoder[Seq[Rate]].decodeJson(s).getOrElse(Seq()))
      Right(seq.getOrElse(Seq()))
    }

  private[this] def quotesRequest(pairs: Seq[Rate.Pair]): Uri =
    Uri(config.baseUri)
      .withPath(Path("/1.0.3/quotes"))
      .withQuery(Query(("pairs", pairs.map(concat).mkString(",")), ("api_key", config.apiKey)))

  private[this] def concat(pair: Rate.Pair): String = s"${pair.from.entryName}${pair.to.entryName}"
}

case class Cached[R] private[oneforge] (
    delegate: Algebra[Eff[R, ?]],
    cacheConfig: CacheConfig
)(
    implicit
    m1: _task[R]
) extends Algebra[Eff[R, ?]] {
  private[this] val cache = MVar[(Instant, Map[Rate.Pair, Rate])]((Instant.EPOCH, Map.empty))

  override def getAll(pairs: Seq[Rate.Pair]): Eff[R, Error Either Seq[Rate]] =
    for {
      c ← fromTask(cache.read)
      rates ← if (isCacheValid(c._1)) fromTask(Task.pure(c._2)) else refreshCache
      rate = pairs.map(rates.get).filter(_.isDefined).map(_.get)
    } yield Right(rate)

  private val refreshCache: Eff[R, Map[Rate.Pair, Rate]] = {
    val allPairs = for {
      from ← Currency.values
      to ← Currency.values
    } yield Rate.Pair(from, to)

    for {
      allRates ← delegate.getAll(allPairs)
      map = allRates.map(s ⇒ s.map(r ⇒ (r.pair, r)).toMap).getOrElse(Map.empty)
      _ = println(map)
      _ ← fromTask(cache.take)
      _ ← fromTask(cache.put((Instant.now(), map)))
    } yield map
  }

  private def isCacheValid(instant: Instant): Boolean =
    instant.isAfter(Instant.now().minusNanos(cacheConfig.ttl.toNanos))
}
