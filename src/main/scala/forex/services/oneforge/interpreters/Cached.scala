package forex.services.oneforge.interpreters

import java.time.{ Clock, Instant }

import forex.config.CacheConfig
import forex.domain.Rate
import forex.main.{ Cache, CacheState }
import forex.services.oneforge.{ Algebra, Error }
import org.atnos.eff.Eff
import org.atnos.eff.addon.monix.task.{ _task, fromTask }

final class Cached[R] private[oneforge] (
    delegate: Algebra[Eff[R, ?]],
    cacheConfig: CacheConfig,
    clock: Clock,
    cache: Cache
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

  private[this] def caching(lastCached: CacheState): Eff[R, Error Either Map[Rate.Pair, Rate]] =
    if (isCacheValid(lastCached._1))
      fromTask(cache.put(lastCached).map(_ ⇒ Right(lastCached._2)))
    else {
      for {
        allRates ← delegate.getAll(Rate.Pair.allPairs)
        map = allRates.map(s ⇒ s.map(r ⇒ (r.pair, r)).toMap)
        _ ← fromTask(map.map(m ⇒ cache.put((clock.instant(), m))).getOrElse(cache.put(lastCached)))
      } yield map
    }

  private[this] def isCacheValid(cached: Instant): Boolean =
    cached.isAfter(clock.instant().minusNanos(cacheConfig.ttl.toNanos))
}
