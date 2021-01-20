package com.lyh.test.server

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model._

import scala.concurrent.Future
import scala.io.StdIn
import scala.util.{Failure, Success}

/** *
 *
 * @description:
 * @author: lyh
 * @date: 2021/1/20
 * @version: v1.0
 */
object HttpServer {

  implicit val system = ActorSystem(Behaviors.empty, "lowlevel")
  implicit val execute = system.executionContext

  private def httpClientSingleRequest: Unit = {
    val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = "http://akka.io"))
    responseFuture.onComplete {
      case Success(res) => println(res)
      case Failure(e) => sys.error("something wrong!"); println(e)
    }
  }

  /**
   * 低价http的api
   * curl http://localhost:8080/
   * curl http://localhost:8080/ping
   * curl http://localhost:8080/crash
   * curl http://localhost:8080/aa
   */
  private def httpServerLowLevel: Unit = {

    // HttpRequest(meth, path, headers, entity, protocol)
    // 此处的requestHandler是一个变量，这个变量的类型是HttpRequest=>HttpResponse的函数，入参为空
    val requestHandler: HttpRequest => HttpResponse = {
      case HttpRequest(GET, Uri.Path("/"), _, _, _) =>
        HttpResponse(entity = HttpEntity(ContentTypes.`text/html(UTF-8)`, "<html><body>Hello world!</body></html>"))
      case HttpRequest(GET, Uri.Path("/ping"), _, _, _) => HttpResponse(entity = "PONG!")
      case HttpRequest(GET, Uri.Path("/crash"), _, _, _) => sys.error("BOOM! 5xx")
      case r: HttpRequest =>
        r.discardEntityBytes()
        HttpResponse(404, entity = "Unkown resource!")
    }
    val bindingFuture = Http().newServerAt("localhost", 8080).bindSync(requestHandler)
    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind) // 解除绑定
      .onComplete(_ => system.terminate) // 关闭服务
  }

  def main(args: Array[String]): Unit = {
    //    httpServerWithActorInteraction
    //    httpClientSingleRequest
    //    httpServerLowLevel


  }
}
