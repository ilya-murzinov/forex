package forex.processes.rates

import forex.services._

object converters {
  import messages._

  def toProcessError[T <: Throwable](t: T): Error = t match {
    case OneForgeError.ExternalApi(_) ⇒ Error.ExternalApi
    case OneForgeError.JsonParsing(_) ⇒ Error.ExternalApi
    case e                            ⇒ Error.System(e)
  }
}
