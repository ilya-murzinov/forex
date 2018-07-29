package forex.domain

import enumeratum._
import io.circe._

sealed trait Currency extends EnumEntry

object Currency extends Enum[Currency] {
  val values = findValues

  final case object AUD extends Currency
  final case object CAD extends Currency
  final case object CHF extends Currency
  final case object EUR extends Currency
  final case object GBP extends Currency
  final case object NZD extends Currency
  final case object JPY extends Currency
  final case object SGD extends Currency
  final case object USD extends Currency

  implicit val encoder: Encoder[Currency] =
    Encoder.encodeString.contramap(_.entryName)

  implicit val decoder: Decoder[Currency] =
    Decoder.decodeString.emap(s ⇒ Currency.withNameOption(s).toRight(s"'$s' is not a currency!"))

}
