package com.lyh.test.server

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives.{complete, get, path}

import scala.io.StdIn

/** *
 *
 * @description: 使用akka在本地发起一个get请求
 * @author: lyh
 * @date: 2021/1/20
 * @version: v1.0
 */
object Test {
  def main(args: Array[String]): Unit = {
    firstAkka

  }

  def firstPostAkka(): Unit ={
    implicit val system = ActorSystem(Behaviors.empty,"my-post")
    implicit val executionContext = system.executionContext
    val route = path("")
  }
  /**
   * 第一个akka任务，提交了一个get请求
   */
  def firstAkka(): Unit = {
    // 用于运行route
    implicit val system = ActorSystem(Behaviors.empty, "my-system")
    // future在使用flatMap等方法的时候需要这个执行环境
    implicit val executionContext = system.executionContext
    // 此处的path("hello")指定路径为根路径下的hello
    val route = path("hello")(get(complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))))
    val bindingFuture = Http().newServerAt("localhost", 8080).bind(route)
    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine()
    bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate())
  }
}
