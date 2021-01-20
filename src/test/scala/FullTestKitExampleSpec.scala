
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.server._
import Directives._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class FullTestKitExampleSpec extends AnyWordSpec with Matchers with ScalatestRouteTest {
  //  path("ping")(complete("pong !")
  val smallRoute = get {
    concat(
      pathSingleSlash(complete("Captain on the bridge")),
      path("ping")(complete("pong !")))
  }
  "The service" should {
    "return a greeting for GET requests to the root path" in {
      Get() ~> smallRoute ~> check {
        responseAs[String] shouldEqual "Captain on the bridge"
      }
    }
    "return a 'PONG!' response for GET requests to /ping" in {
      Get("/ping") ~> smallRoute ~> check{
        responseAs[String] shouldEqual "pong !"
      }
    }
    "leave GET requests to other paths unhandled" in {
      // tests:
      Get("/kermit") ~> smallRoute ~> check {
        handled shouldBe false
      }
    }

    "return a MethodNotAllowed error for PUT requests to the root path" in {
      // tests:
      Put() ~> Route.seal(smallRoute) ~> check {
        status shouldEqual StatusCodes.MethodNotAllowed
        responseAs[String] shouldEqual "HTTP method not allowed, supported methods: GET"
      }
    }
  }

}
