package forex.services.oneforge.interpreters

import forex.domain.{ Price, Rate, Timestamp }
import forex.services.oneforge.{ Algebra, Error }
import monix.eval.Task
import org.atnos.eff.Eff
import org.atnos.eff.addon.monix.task.{ _task, fromTask }

final class Dummy[R] private[oneforge] (implicit m1: _task[R]) extends Algebra[Eff[R, ?]] {
  override def getAll(pairs: Seq[Rate.Pair]): Eff[R, Error Either Seq[Rate]] =
    for {
      result ← fromTask(Task.now(pairs.map(dummyRate)))
    } yield Right(result)

  private[this] def dummyRate(pair: Rate.Pair) = Rate(pair, Price(BigDecimal(100)), Timestamp.now)
}
