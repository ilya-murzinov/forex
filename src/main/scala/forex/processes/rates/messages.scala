package forex.processes.rates

import forex.domain._
import scala.util.control.NoStackTrace

object messages {

  sealed trait Error extends Throwable with NoStackTrace
  object Error {
    final case class NotFound(pair: Rate.Pair) extends Error
    final case object ExternalApi extends Error
    final case class System(underlying: Throwable) extends Error
  }

  final case class GetRequest(
      from: Currency,
      to: Currency
  )
}
