package forex.processes.rates

import cats.Monad
import cats.data.EitherT
import forex.domain._
import forex.services._

object Processes {
  def apply[F[_]]: Processes[F] =
    new Processes[F] {}
}

trait Processes[F[_]] {
  import converters._
  import messages._

  def get(request: GetRequest)(implicit M: Monad[F], OneForge: OneForge[F]): F[Error Either Rate] = {
    val pair = Rate.Pair(request.from, request.to)
    EitherT(OneForge.getAll(Seq(pair)))
      .leftMap(toProcessError)
      .flatMap[Error, Rate] { seq ⇒
        seq.filter(r ⇒ r.pair == pair) match {
          case Seq(rate) ⇒ EitherT.pure(rate)
          case _         ⇒ EitherT.fromEither(Left(Error.NotFound))
        }
      }
      .value
  }
}
