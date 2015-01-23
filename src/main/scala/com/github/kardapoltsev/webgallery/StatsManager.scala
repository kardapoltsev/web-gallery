package com.github.kardapoltsev.webgallery

import akka.actor.{ ActorLogging, Actor }
import akka.event.LoggingReceive
import com.github.kardapoltsev.webgallery.CommentManager._
import com.github.kardapoltsev.webgallery.StatsManager.{ GetStatsResponse, GetStats }
import com.github.kardapoltsev.webgallery.db._
import com.github.kardapoltsev.webgallery.http.ApiRequest
import com.github.kardapoltsev.webgallery.http.{ ApiRequest, ApiResponse, AuthorizedRequest }
import com.github.kardapoltsev.webgallery.routing.{ StatsManagerRequest, CommentManagerRequest }
import scalikejdbc.DB
import spray.json.DefaultJsonProtocol

/**
 * Created by koko on 21/01/15.
 */
object StatsManager extends DefaultJsonProtocol {

  case class GetStats()
    extends ApiRequest with StatsManagerRequest

  case class GetStatsResponse(stats: Stats) extends ApiResponse
  object GetStatsResponse {
    implicit val _ = jsonFormat1(GetStatsResponse.apply)
  }
}

class StatsManager extends Actor with ActorLogging {

  import com.github.kardapoltsev.webgallery.http.marshalling._
  import context.dispatcher

  def receive = LoggingReceive(
    Seq(processGetStats) reduceLeft (_ orElse _)
  )

  private def processGetStats: Receive = {
    case r @ GetStats() =>
      val stats = DB readOnly { implicit s =>
        Stats(User.countAll, Image.countAll, Comment.countAll, Like.countAll)
      }
      r.complete(GetStatsResponse(stats))
  }
}