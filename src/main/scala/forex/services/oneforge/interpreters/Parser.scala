package forex.services.oneforge.interpreters

import forex.domain.{ Currency, Price, Rate, Timestamp }
import forex.services.oneforge.Error
import forex.services.oneforge.Error.JsonParsing
import io.circe.{ Decoder, DecodingFailure, HCursor }

object Parser {
  private[this] implicit val decoder: Decoder[Rate] = (c: HCursor) ⇒
    for {
      pair ← c.downField("symbol").as[String].flatMap { s ⇒
        (for {
          symbol ← if (s.length == 6) Some(s) else None
          from ← Currency.withNameOption(symbol.substring(0, 3))
          to ← Currency.withNameOption(symbol.substring(3, 6))
        } yield Rate.Pair(from, to)).toRight(DecodingFailure(s"Symbol '$s' is invalid!", List()))
      }
      price ← c.downField("price").as[BigDecimal]
      ts ← c.downField("timestamp").as[Long]
    } yield Rate(pair, Price(price), Timestamp.ofEpochSecond(ts))

  def parse(json: String): Error Either Seq[Rate] =
    (for {
      parsed ← io.circe.jawn.parse(json)
      decoded ← Decoder[Seq[Rate]].decodeJson(parsed)
    } yield decoded).left.map(_ ⇒ JsonParsing(json))
}
