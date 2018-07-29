package forex.services.oneforge.interpreters

import forex.BaseSpec
import forex.domain.Currency._
import forex.domain.{ Price, Rate, Timestamp }
import forex.services.oneforge.Error.JsonParsing

class ParserSpec extends BaseSpec {
  behavior of "Parser"

  it should "parse correct response" in {
    val ts = Timestamp.ofEpochSecond(1532823782)
    val json =
      s"""
        |[
        |  {
        |    "symbol": "EURUSD",
        |    "bid": 1.16556,
        |    "ask": 1.16582,
        |    "price": 1.16569,
        |    "timestamp": ${ts.value.toEpochSecond}
        |  },
        |  {
        |    "symbol": "GBPJPY",
        |    "bid": 145.427,
        |    "ask": 145.584,
        |    "price": 145.5055,
        |    "timestamp": ${ts.value.toEpochSecond}
        |  },
        |  {
        |    "symbol": "AUDUSD",
        |    "bid": 0.74002,
        |    "ask": 0.74026,
        |    "price": 0.74014,
        |    "timestamp": ${ts.value.toEpochSecond}
        |  }
        |]
      """.stripMargin

    val result = Seq(
      Rate(Rate.Pair(EUR, USD), Price(1.16569), ts),
      Rate(Rate.Pair(GBP, JPY), Price(145.5055), ts),
      Rate(Rate.Pair(AUD, USD), Price(0.74014), ts)
    )

    Parser.parse(json) shouldBe Right(result)
  }

  it should "fail if response is incorrect" in {
    Seq(
      "WAT",
      "{}",
      """
        |{
        |  "symbol": "WAT",
        |  "bid": 1.16556,
        |  "ask": 1.16582,
        |  "price": 1.16569,
        |  "timestamp": 1532823782
        |}
      """.stripMargin,
      """
        |{
        |  "symbol": "USBGBP",
        |  "bid": 1.16556,
        |  "ask": 1.16582,
        |  "price": 1.16569,
        |  "timestamp": 1532823782
        |}
      """.stripMargin,
      """
        |{
        |  "symbol": "USDGBP",
        |  "bid": 1.16556,
        |  "ask": 1.16582,
        |  "price": "WAT",
        |  "timestamp": 1532823782
        |}
      """.stripMargin
    ).foreach { json ⇒
      Parser.parse(json) shouldBe Left(JsonParsing(json))
    }
  }
}
