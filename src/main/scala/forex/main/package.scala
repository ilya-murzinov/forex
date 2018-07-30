package forex

import java.time.Instant

import forex.domain.Rate
import monix.eval.{ MVar, Task }
import org.atnos.eff._
import org.zalando.grafter._

package object main {

  type AppStack = Fx.fx1[Task]
  type AppEffect[R] = Eff[AppStack, R]

  type CacheState = (Instant, Map[Rate.Pair, Rate])
  type Cache = MVar[CacheState]

  def toStartErrorString(results: List[StartResult]): String =
    s"Application startup failed. Modules: ${results
      .collect {
        case StartError(message, ex) ⇒ s"$message [${ex.getMessage}]"
        case StartFailure(message)   ⇒ message
      }
      .mkString(", ")}"

  def toStartSuccessString(results: List[StartResult]): String =
    s"Application startup successful. Modules: ${results
      .collect {
        case StartOk(message) ⇒ message
      }
      .mkString(", ")}"

}
