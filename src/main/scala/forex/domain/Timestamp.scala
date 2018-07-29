package forex.domain

import io.circe._
import io.circe.generic.extras.wrapped._
import io.circe.java8.time._
import java.time.{ Instant, OffsetDateTime, ZoneOffset }

case class Timestamp(value: OffsetDateTime) extends AnyVal

object Timestamp {
  def now: Timestamp =
    Timestamp(OffsetDateTime.now)

  def ofEpochMilli(epochMillis: Long) =
    Timestamp(OffsetDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneOffset.UTC))

  implicit val encoder: Encoder[Timestamp] = deriveUnwrappedEncoder[Timestamp]

  implicit val decoder: Decoder[Timestamp] = deriveUnwrappedDecoder[Timestamp]
}
