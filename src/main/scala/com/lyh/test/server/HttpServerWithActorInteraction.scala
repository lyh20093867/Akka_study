package com.lyh.test.server

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.util.Timeout
import spray.json.DefaultJsonProtocol._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.io.StdIn

/** *
 *
 * @description:
 * @author: lyh
 * @date: 2021/1/20
 * @version: v1.0
 */
object HttpServerWithActorInteraction {

  object Auction {

    // 定义一个封装的message特质，该特质有且仅有Bid、Bids、GetBids这三个实现类
    sealed trait Message

    case class Bid(userId: String, offer: Int) extends Message

    case class Bids(bids: List[Bid])

    case class GetBids(replyTo: ActorRef[Bids]) extends Message

    def apply: Behaviors.Receive[Message] = apply(List.empty)

    def apply(bids: List[Bid]): Behaviors.Receive[Message] = Behaviors.receive {
      case (ctx, bid@Bid(userId, offer)) => {
        ctx.log.info(s"Bid complete:$userId,$offer")
        apply(bids :+ bid)
        //        apply(bid +: bids)
      }
      case (_, GetBids(replyTo)) =>
        replyTo ! Bids(bids)
        Behaviors.same
    }
  }

  implicit val bidFormat = jsonFormat2(Auction.Bid)
  implicit val bidsFormat = jsonFormat1(Auction.Bids)

  /**
   * curl -X PUT "http://localhost:8080/auction?bid=22&user=MartinO"
   * curl http://localhost:8080/auction
   * 网页访问http://localhost:8080/auction
   */
  private def httpServerWithActorInteraction = {
    implicit val system: ActorSystem[Auction.Message] = ActorSystem(Auction.apply, "auction")
    implicit val execute: ExecutionContext = system.executionContext

    val auction: ActorRef[Auction.Message] = system
    import Auction._
    val route = path("auction") {
      concat(
        put {
          parameters("bid".as[Int], "user") { (bid, user) =>
            auction ! Bid(user, bid)
            complete(StatusCodes.Accepted, "bid placed")
          }
        },
        get {
          implicit val timeout: Timeout = 5.seconds
          val bids: Future[Bids] = (auction ? GetBids).mapTo[Bids]
          complete(bids)
        }
      )
    }
    val bindingFuture = Http().newServerAt("localhost", 8080).bind(route)
    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine()
    bindingFuture.flatMap(_.unbind).onComplete(_ => system.terminate)
  }

  def main(args: Array[String]): Unit = {
    httpServerWithActorInteraction

  }
}
