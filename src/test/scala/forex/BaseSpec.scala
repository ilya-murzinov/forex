package forex

import forex.main.AppStack
import monix.eval.Task
import org.atnos.eff.Eff
import org.atnos.eff.syntax.addon.monix.task.toTaskOps
import org.scalamock.scalatest.MockFactory
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{ FlatSpec, Matchers }

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

trait BaseSpec extends FlatSpec with Matchers with GeneratorDrivenPropertyChecks with MockFactory {

  implicit class EffOps[A](val e: Eff[AppStack, A]) {
    val run: A = e.runAsync.run
  }

  implicit class TaskOps[A](val e: Task[A]) {
    import monix.execution.Scheduler.Implicits.global

    val run: A = Await.result(e.runAsync, 5.seconds)
  }
}
