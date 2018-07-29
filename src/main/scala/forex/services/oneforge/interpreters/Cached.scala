package forex.services.oneforge.interpreters

import java.time.{ Clock, Instant }

import forex.config.CacheConfig
import forex.domain.Rate
import forex.services.oneforge.{ Algebra, Error }
import monix.eval.MVar
import org.atnos.eff.Eff
import org.atnos.eff.addon.monix.task.{ _task, fromTask }

final class Cached[R] private[oneforge] (
    delegate: Algebra[Eff[R, ?]],
    cacheConfig: CacheConfig,
    clock: Clock = Clock.systemUTC(),
    cache: MVar[(Instant, Map[Rate.Pair, Rate])] = MVar[(Instant, Map[Rate.Pair, Rate])]((Instant.EPOCH, Map.empty))
)(
    implicit
    m1: _task[R]
) extends Algebra[Eff[R, ?]] {

  override def getAll(pairs: Seq[Rate.Pair]): Eff[R, Error Either Seq[Rate]] =
    for {
      c ← fromTask(cache.take)
      rates ← caching(c)
      rate = rates.map(r ⇒ pairs.map(r.get).filter(_.isDefined).map(_.get))
    } yield rate

  private[this] def caching(
      c: (Instant, Map[Rate.Pair, Rate])
  ): Eff[R, Error Either Map[Rate.Pair, Rate]] =
    if (isCacheValid(c._1))
      fromTask(cache.put(c).map(_ ⇒ Right(c._2)))
    else {
      for {
        allRates ← delegate.getAll(Rate.Pair.allPairs)
        map = allRates.map(s ⇒ s.map(r ⇒ (r.pair, r)).toMap)
        _ ← fromTask(map.map(m ⇒ cache.put((clock.instant(), m))).getOrElse(cache.put(c)))
      } yield map
    }

  private[this] def isCacheValid(instant: Instant): Boolean =
    instant.isAfter(clock.instant().minusNanos(cacheConfig.ttl.toNanos))
}
