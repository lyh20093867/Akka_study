package com.lyh.test.server

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.stream.scaladsl._
import akka.util.ByteString
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._

import scala.util.Random
import scala.io.StdIn

/** *
 *
 * @description:
 * @author: lyh
 * @date: 2021/1/20
 * @version: v1.0
 */
object HttpServerStreamingRandomNumbers {
  /**
   * curl --limit-rate 50b 127.0.0.1:8080/random
   * 此处应访问 http://localhost:8080/random
   */
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem(Behaviors.empty, "RandomNumbers")
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.executionContext

    val numbers = Source.fromIterator(() => Iterator.continually(Random.nextInt()))
    HttpEntity(ContentTypes.`text/plain(UTF-8)`, Array[Byte]())
    val route = path("random") {
      get {
        complete(
          HttpEntity(
            ContentTypes.`text/plain(UTF-8)`,
            numbers.map(n => ByteString(s"$n\n"))
          )
        )
      }
    }
    val bindingFuture = Http().newServerAt("localhost", 8080).bind(route)
    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine()
    bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate())
  }
}
