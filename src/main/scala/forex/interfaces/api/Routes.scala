package forex.interfaces.api

import akka.http.scaladsl._
import forex.config._
import forex.interfaces.api.utils._
import org.zalando.grafter.macros._

@readerOf[ApplicationEnvironment]
case class Routes(ratesRoutes: rates.Routes) {
  import server.Directives._

  lazy val route: server.Route =
    pathPrefix("api" / "v1") {
      handleExceptions(ApiExceptionHandler()) {
        handleRejections(ApiRejectionHandler()) {
          pathPrefix("rate") { ratesRoutes.route }
        }
      }
    }
}
