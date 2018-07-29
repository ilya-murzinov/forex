package forex.config

import org.zalando.grafter.macros._

import scala.concurrent.duration.FiniteDuration

@readers
case class ApplicationConfig(
    akka: AkkaConfig,
    api: ApiConfig,
    executors: ExecutorsConfig,
    oneForge: OneForgeConfig,
    cache: CacheConfig,
    dummyInterpreter: Boolean
)

case class AkkaConfig(
    name: String,
    exitJvmTimeout: Option[FiniteDuration]
)

case class ApiConfig(
    interface: String,
    port: Int
)

case class ExecutorsConfig(
    default: String
)

case class OneForgeConfig(
    baseUri: String,
    apiKey: String,
    readTimeout: FiniteDuration
)

case class CacheConfig(
    ttl: FiniteDuration
)
