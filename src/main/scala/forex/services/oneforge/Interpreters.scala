package forex.services.oneforge

import java.time.Clock

import forex.config.{ CacheConfig, OneForgeConfig }
import forex.main
import forex.main.{ ActorSystems, Executors }
import forex.services.oneforge.interpreters._
import org.atnos.eff._
import org.atnos.eff.addon.monix.task._

object Interpreters {

  def dummy[R](implicit m1: _task[R]): Algebra[Eff[R, ?]] = new Dummy[R]

  def real[R](
      oneForgeConfig: OneForgeConfig,
      executors: Executors,
      actorSystems: ActorSystems
  )(
      implicit
      m1: _task[R]
  ): Algebra[Eff[R, ?]] =
    new OneForgeClient[R](oneForgeConfig, actorSystems, executors)

  def cached[R](
      cacheConfig: CacheConfig,
      clock: Clock,
      delegate: Algebra[Eff[R, ?]],
      cache: main.Cache
  )(
      implicit
      m1: _task[R]
  ): Algebra[Eff[R, ?]] = new Cached[R](delegate, cacheConfig, clock, cache)
}
