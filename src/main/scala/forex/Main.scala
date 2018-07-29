package forex

import cats.Eval
import com.typesafe.scalalogging._
import forex.config._
import forex.main._
import monix.eval.Task
import org.zalando.grafter._

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

object Main extends LazyLogging {

  val app: Task[Option[Application]] = {
    Task.eval(pureconfig.loadConfig[ApplicationConfig]("app") match {
      case Left(errors) ⇒
        logger.error(s"Errors loading the configuration:\n${errors.toList.mkString("- ", "\n- ", "")}")
        None
      case Right(applicationConfig) ⇒
        val application = configure[Application](applicationConfig).configure()

        Rewriter
          .startAll(application)
          .flatMap {
            case results if results.exists(!_.success) ⇒
              logger.error(toStartErrorString(results))
              Rewriter
                .stopAll(application)
                .map(_ ⇒ ())
                .map(_ ⇒ None)
            case results ⇒
              logger.info(toStartSuccessString(results))
              Eval.now(Some(application))
          }
          .value
    })
  }

  def main(args: Array[String]): Unit = {
    import monix.execution.Scheduler.Implicits.global

    val app1 = Await.result(app.runAsync, 10.seconds)
    sys.addShutdownHook {
      app1.foreach { g ⇒
        Rewriter.stopAll(g)
      }
    }
  }
}
