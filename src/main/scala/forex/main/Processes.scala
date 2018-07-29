package forex.main

import forex.config._
import forex.{ processes ⇒ p, services ⇒ s }
import org.zalando.grafter.macros._

@readerOf[ApplicationConfig]
case class Processes(
    oneForgeConfig: OneForgeConfig,
    executors: Executors,
    cache: CacheConfig,
    dummyInterpreter: Boolean,
    actorSystems: ActorSystems
) {

  implicit final lazy val _oneForge: s.OneForge[AppEffect] =
    if (dummyInterpreter)
      s.OneForge.dummy[AppStack]
    else
      s.OneForge.cached[AppStack](cache, s.OneForge.real[AppStack](oneForgeConfig, executors, actorSystems))

  final val Rates = p.Rates[AppEffect]

}
