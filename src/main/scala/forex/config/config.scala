package forex.config

import java.time.Clock

import forex.main.Cache
import org.zalando.grafter.macros._

import scala.concurrent.duration.FiniteDuration

@readers
case class ApplicationEnvironment(
    config: ApplicationConfig,
    cache: Cache,
    clock: Clock
)

@readers
case class ApplicationConfig(
    akka: AkkaConfig,
    api: ApiConfig,
    executors: ExecutorsConfig,
    oneForge: OneForgeConfig,
    cacheConfig: CacheConfig,
    dummyInterpreter: Boolean,
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
