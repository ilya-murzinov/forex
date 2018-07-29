package forex

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport
import forex.domain.Currency.GBP
import forex.domain.{ Currency, Price, Rate }
import forex.interfaces.api.rates.Protocol._
import forex.main.Application
import org.scalacheck.Gen
import org.scalatest.prop.GeneratorDrivenPropertyChecks

class MainIntegrationTest
    extends BaseSpec
    with ScalatestRouteTest
    with ErrorAccumulatingCirceSupport
    with GeneratorDrivenPropertyChecks {

  behavior of "App"

  val app: Application = Main.app.run.fold(fail("App does not exist"))(identity)

  val allPairs: Gen[Rate.Pair] = for {
    from ← Gen.oneOf(Currency.values)
    to ← Gen.oneOf(Currency.values)
  } yield Rate.Pair(from, to)

  it should "return dummy rates for all currency pairs" in forAll(allPairs) { pair ⇒
    Get(s"/api/v1/rate?from=${pair.from}&to=${pair.to}") ~> app.api.routes.route ~> check {
      response.status shouldBe StatusCodes.OK
      val res = responseAs[GetApiResponse]

      res shouldBe a[GetApiResponse]
      res.from shouldBe pair.from
      res.to shouldBe pair.to
      if (pair.isMono)
        res.price shouldBe Price(1.0)
      else
        res.price shouldBe Price(100.0)
    }
  }

  it should "return 400 error if 'to' is invalid" in forAll(Gen.numStr) { invalid ⇒
    Get(s"/api/v1/rate?from=$GBP&to=$invalid") ~> app.api.routes.route ~> check {
      response.status shouldBe StatusCodes.BadRequest
    }
  }

  it should "return 400 error if 'from' is invalid" in forAll(Gen.numStr) { invalid ⇒
    Get(s"/api/v1/rate?from=$invalid&to=$GBP") ~> app.api.routes.route ~> check {
      response.status shouldBe StatusCodes.BadRequest
    }
  }
}
