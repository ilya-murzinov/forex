package forex.interfaces.api.utils

import akka.http.scaladsl._
import com.typesafe.scalalogging.LazyLogging
import forex.processes._

object ApiExceptionHandler extends LazyLogging {

  def apply(): server.ExceptionHandler =
    server.ExceptionHandler {
      case RatesError.NotFound(pair) ⇒
        ctx ⇒
          ctx.complete(s"Rate for '$pair' not found")
      case RatesError.ExternalApi ⇒
        ctx ⇒
          ctx.complete(s"Something went wrong with external API call")
      case e: Throwable ⇒
        ctx ⇒
          val msg = "Something went wrong"
          logger.error(msg, e)
          ctx.complete(msg)
    }
}
