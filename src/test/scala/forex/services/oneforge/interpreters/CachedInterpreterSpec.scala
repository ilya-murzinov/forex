package forex.services.oneforge.interpreters

import java.time.temporal.ChronoUnit
import java.time.{ Clock, Instant, ZoneOffset }

import forex.BaseSpec
import forex.config.CacheConfig
import forex.domain.Currency.{ GBP, USD }
import forex.domain.{ Price, Rate, Timestamp }
import forex.main.{ AppStack, CacheState }
import forex.services.oneforge.Algebra
import monix.eval.MVar
import org.atnos.eff.Eff
import org.atnos.eff.addon.monix.task._

import scala.concurrent.duration.DurationLong

class CachedInterpreterSpec extends BaseSpec {

  private val now = Instant.now()
  private val clock = Clock.fixed(now, ZoneOffset.UTC)
  private val cache = MVar[CacheState]((Instant.EPOCH, Map.empty)).run
  private val ttl = 2.minutes
  private val delegate = mock[Algebra[Eff[AppStack, ?]]]
  private val subj = new Cached[AppStack](delegate, CacheConfig(ttl), clock, cache)

  private val pair = Rate.Pair(GBP, USD)
  private val rate = Rate(pair, Price(100.0), Timestamp.ofEpochSecond(clock.millis()))

  behavior of "Cached interpreter"

  it should "call delegate when cache is invalid" in {
    (delegate.getAll _).expects(Rate.Pair.allPairs).returning(Eff.pure(Right(Seq(rate))))

    (for {
      result ← subj.getAll(Seq(pair))
      c ← fromTask(cache.read)
    } yield {
      c._1 shouldBe clock.instant()
      c._2 shouldBe Map(pair → rate)
      result shouldBe Right(Seq(rate))
    }).run
  }

  it should "call not delegate when cache is valid" in {
    (for {
      _ ← fromTask(cache.take)
      _ ← fromTask(cache.put((clock.instant().minus(1, ChronoUnit.MINUTES), Map(pair → rate))))
      result ← subj.getAll(Seq(pair))
    } yield {
      result shouldBe Right(Seq(rate))
    }).run
  }
}
