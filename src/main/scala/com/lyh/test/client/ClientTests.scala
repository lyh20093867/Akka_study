package com.lyh.test.client

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest}
import scala.concurrent.duration._

/** *
 *
 * @description: 通过akka，使用post请求application/x-www-form-urlencoded类型的数据
 * @author: lyh
 * @date: 2021/1/20
 * @version: v1.0
 */
object ClientTests {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem(Behaviors.empty, "post")
    implicit val executionContext = system.executionContext
    val entitys = HttpEntity(ContentTypes.`application/x-www-form-urlencoded`, s"""postJson=[{\"exposure\":99999,\"feedUrl\":\"www.baidu.com\",\"thridId\":11111},{\"exposure\":22222,\"feedUrl\":\"www.moji.com\",\"thridId\":222}]""")
    val request = HttpRequest(method = HttpMethods.POST, uri = "http://mpc.matrixback.com/sns_admin/feed/thrid/add.jhtml", entity = entitys)
    val responseFuture = Http().singleRequest(request)
    responseFuture.flatMap(_.entity.toStrict(2.seconds)).map(_.data.utf8String).foreach(println)
    responseFuture.onComplete(_ => system.terminate())
  }
}
