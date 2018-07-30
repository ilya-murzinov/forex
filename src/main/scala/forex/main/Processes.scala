package forex.main

import java.time.Clock

import forex.config._
import forex.{ processes ⇒ p, services ⇒ s }
import org.zalando.grafter.macros._

@readerOf[ApplicationEnvironment]
case class Processes(
    oneForgeConfig: OneForgeConfig,
    executors: Executors,
    cacheConfig: CacheConfig,
    dummyInterpreter: Boolean,
    actorSystems: ActorSystems,
    cache: Cache,
    clock: Clock
) {

  implicit final lazy val _oneForge: s.OneForge[AppEffect] =
    if (dummyInterpreter)
      s.OneForge.dummy[AppStack]
    else {
      val real = s.OneForge.real[AppStack](oneForgeConfig, executors, actorSystems)
      s.OneForge.cached[AppStack](cacheConfig, clock, real, cache)
    }

  final val Rates = p.Rates[AppEffect]

}
