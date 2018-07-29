package forex.services.oneforge

import forex.config.{ CacheConfig, OneForgeConfig }
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
      delegate: Algebra[Eff[R, ?]]
  )(
      implicit
      m1: _task[R]
  ): Algebra[Eff[R, ?]] = new Cached[R](delegate, cacheConfig)
}
