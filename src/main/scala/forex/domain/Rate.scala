package forex.domain

import io.circe._
import io.circe.generic.semiauto._

case class Rate(
    pair: Rate.Pair,
    price: Price,
    timestamp: Timestamp
)

object Rate {
  final case class Pair(
      from: Currency,
      to: Currency
  ) {
    def isMono: Boolean = from == to
  }

  object Pair {
    val allPairs = for {
      from ← Currency.values
      to ← Currency.values
      if from != to
    } yield Rate.Pair(from, to)

    implicit val encoder: Encoder[Pair] =
      deriveEncoder[Pair]
  }

  implicit val encoder: Encoder[Rate] =
    deriveEncoder[Rate]
}
