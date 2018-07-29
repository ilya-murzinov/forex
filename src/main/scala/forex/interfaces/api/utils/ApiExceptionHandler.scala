package forex.interfaces.api.utils

import akka.http.scaladsl._
import com.typesafe.scalalogging.LazyLogging
import forex.processes._

object ApiExceptionHandler extends LazyLogging {

  def apply(): server.ExceptionHandler =
    server.ExceptionHandler {
      case e: RatesError ⇒
        ctx ⇒
          val msg = "Something went wrong in the rates process"
          logger.error(msg, e)
          ctx.complete(msg)
      case e: Throwable ⇒
        ctx ⇒
          val msg = "Something else went wrong"
          logger.error(msg, e)
          ctx.complete(msg)
    }
}
