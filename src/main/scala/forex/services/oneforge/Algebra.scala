package forex.services.oneforge

import forex.domain._

trait Algebra[F[_]] {
  def getAll(pairs: Seq[Rate.Pair]): F[Error Either Seq[Rate]]
}
