package com.lyh.test.server

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.Done
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
// for JSON serialization/deserialization following dependency is required:
// "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.7"
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._

import scala.io.StdIn

import scala.concurrent.Future

/** *
 *
 * @description:
 * @author: lyh
 * @date: 2021/1/20
 * @version: v1.0
 */
object SprayJsonExample {
  implicit val system = ActorSystem(Behaviors.empty, "my-system")
  implicit val execute = system.executionContext

  var orders: List[Item] = Nil

  final case class Item(name: String, id: Long)

  final case class Order(items: List[Item])

  implicit val itemFormat = jsonFormat2(Item)
  implicit val orderFormat = jsonFormat1(Order)

  def fetchItem(itemId: Long): Future[Option[Item]] = Future(orders.find(_.id == itemId))

  def saveOrder(order: Order): Future[Done] = {
    orders = order match {
      case Order(items) => items ::: orders
      case _ => orders
    }
    Future(Done)
  }

  /**
   * curl -H "Content-Type: application/json" -X POST -d '{"items":[{"name":"hhgtg","id":42}]}' http://localhost:8080/create-order
   * curl -H "Content-Type: application/json" -X POST -d '{"items":[{"name":"zhangsan","id":43}]}' http://localhost:8080/create-order
   * curl http://localhost:8080/item/42
   */
  private def sprayJsonPostExample = {
    val route: Route = concat(get {
      pathPrefix("item" / LongNumber) { id =>
        val mybeItem: Future[Option[Item]] = fetchItem(id)
        onSuccess(mybeItem) {
          case Some(item) => complete(item)
          case _ => complete(StatusCodes.NotFound)
        }
      }
    }, post {
      path("create-order")(entity(as[Order])(order => {
        val saved: Future[Done] = saveOrder(order)
        onSuccess(saved)(_ => complete("order created"))
      }))
    })
    val bindingFuture = Http().newServerAt("localhost", 8080).bind(route)
    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine()
    bindingFuture.flatMap(_.unbind()) // 此处跟本地的8080端口解除绑定
      .onComplete(_ => system.terminate())
  }

  def main(args: Array[String]): Unit = {
    //    val route = concat(formField("color")(color => complete(s"The color is '$color")), formField("id".as[Int])(id => complete(s"The id is '$id'")))
    //    httpServerStreamingRandomNumbers
    sprayJsonPostExample
  }

}
