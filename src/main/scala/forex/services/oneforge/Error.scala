package forex.services.oneforge

import scala.util.control.NoStackTrace

sealed trait Error extends Throwable with NoStackTrace
object Error {
  final case class JsonParsing(response: String) extends Error
  final case class ExternalApi(underlying: Throwable) extends Error
}
