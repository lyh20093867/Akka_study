import akka.http.scaladsl.server.Directives.{complete, concat}
import akka.http.scaladsl.model.{FormData, StatusCodes}
import akka.http.scaladsl.server.Directives.formField
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import akka.http.scaladsl.model.{HttpRequest, StatusCodes, _}
import akka.http.scaladsl.server.Directives._

import akka.http.scaladsl.server.Route

class Test2 extends AnyWordSpec with Matchers with ScalatestRouteTest {

  val route = concat(formField("color")(color => complete(s"the color is '$color'")),
    formField("id".as[Int])(id => complete(s"the id is '$id'")))
  val formData = FormData(("color" -> "blue"))

  //  Post("/", FormData("color" -> "blue")) ~> route ~> check {
  //    responseAs[String] shouldEqual "The color is 'blue'"
  //  }
  Post("/", FormData("color" -> "blue")) ~> route ~> check {
    responseAs[String] shouldEqual "the color is 'blue'"
  }
  Get("/") ~> Route.seal(route) ~> check {
    status shouldEqual StatusCodes.BadRequest
    responseAs[String] shouldEqual "Request is missing required form field 'color'"
  }
}
