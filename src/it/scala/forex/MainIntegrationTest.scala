package forex

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport
import forex.domain.Currency.GBP
import forex.domain.{ Currency, Price }
import forex.interfaces.api.rates.Protocol._
import forex.main.Application
import org.scalacheck.Gen
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{ FlatSpec, Matchers }

class MainIntegrationTest
    extends FlatSpec
    with Matchers
    with ScalatestRouteTest
    with ErrorAccumulatingCirceSupport
    with GeneratorDrivenPropertyChecks {

  behavior of "App"

  val app: Application = Main.app.fold(fail("App does not exist"))(identity)

  it should "return dummy rates for all currency pairs" in forAll(Gen.oneOf(Currency.values)) { from ⇒
    forAll(Gen.oneOf(Currency.values)) { to ⇒
      Get(s"/api/v1/rate?from=$from&to=$to") ~> app.api.routes.route ~> check {
        response.status shouldBe StatusCodes.OK
        val res = responseAs[GetApiResponse]

        res shouldBe a[GetApiResponse]
        res.from shouldBe from
        res.to shouldBe to
        if (from == to)
          res.price shouldBe Price(1.0)
        else
          res.price shouldBe Price(100.0)
      }
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
