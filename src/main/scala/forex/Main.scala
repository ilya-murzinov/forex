package forex

import java.time.{ Clock, Instant }

import com.typesafe.scalalogging._
import forex.config._
import forex.main._
import monix.eval.{ MVar, Task, TaskApp }
import org.zalando.grafter._

object Main extends TaskApp with LazyLogging {

  val startedApp: Task[(Application, List[StartResult])] = for {
    config ← Task.eval(pureconfig.loadConfig[ApplicationConfig]("app"))
    application ← config match {
      case Left(errors) ⇒
        Task.raiseError(
          new IllegalArgumentException(
            s"Errors loading the configuration:\n${errors.toList.mkString("- ", "\n- ", "")}"
          )
        )
      case Right(applicationConfig) ⇒
        for {
          cache ← MVar[CacheState]((Instant.EPOCH, Map.empty))
          env = ApplicationEnvironment(applicationConfig, cache, Clock.systemUTC())
          configured ← Task.eval(configure[Application](env).configure())
        } yield configured
    }
    results ← Task.fromEval(Rewriter.startAll(application))
  } yield (application, results)

  val app: Task[Unit] =
    startedApp.bracket {
      case (_, results) ⇒
        if (results.exists(!_.success))
          Task.raiseError(new IllegalArgumentException(s"Can't start: ${toStartErrorString(results)}"))
        else Task.eval(logger.info(toStartSuccessString(results))).flatMap(_ ⇒ Task.never[Unit])
    } {
      case (application, _) ⇒
        Task.fromEval(Rewriter.stopAll(application)).map(_ ⇒ ())
    }

  override def runc: Task[Unit] = app
}
