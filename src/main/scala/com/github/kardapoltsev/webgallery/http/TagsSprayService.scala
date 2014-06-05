package com.github.kardapoltsev.webgallery.http

import spray.routing.{Route, HttpService}
import scala.concurrent.{Future, ExecutionContext}
import akka.util.Timeout
import spray.http._
import spray.json._
import com.github.kardapoltsev.webgallery.db.Tag
import com.github.kardapoltsev.webgallery.Database.CreateTag



/**
 * Created by alexey on 6/4/14.
 */
trait TagsSprayService { this: HttpService =>
  import marshalling._
  implicit def executionContext: ExecutionContext
  implicit def requestTimeout: Timeout

  protected def getTags: Future[Seq[Tag]]
  protected def createTag(request: CreateTag): Future[Tag]

  val tagsRoute: Route = respondWithMediaType(MediaTypes.`application/json`) {
    pathPrefix("api") {
      path("tags") {
        get { ctx =>
          getTags map {
            case tags => ctx.complete(tags.toJson.compactPrint)
          }
        } ~
        post {
          entity(as[CreateTag]) { tag => ctx =>
            createTag(tag) map {
              case t => ctx.complete(t.toJson.compactPrint)
            }
          }
        }
      }
    }
  }
}
