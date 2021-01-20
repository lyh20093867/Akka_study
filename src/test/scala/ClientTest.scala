import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import spray.json.DefaultJsonProtocol._

import scala.concurrent.Future

class ClientTest extends AnyWordSpec with Matchers with ScalatestRouteTest {
  HttpRequest(uri = "https://akka.io")

  // or:
  import akka.http.scaladsl.client.RequestBuilding.Get
  Get("https://akka.io")

  // with query params
  Get("https://akka.io?foo=bar")

//  case class Pet(name: String)
//  HttpRequest(
//    method = HttpMethods.POST,
//    uri = "https://userservice.example/users",
//    entity = HttpEntity(ContentTypes.`text/plain(UTF-8)`, "data")
//  )

  // or:
  import akka.http.scaladsl.client.RequestBuilding.Post
  Post("https://userservice.example/users", "data")

//  implicit val petFormat = jsonFormat1(Pet)
//  val pet: Future[Pet] = Unmarshal(response).to[Pet]

}
