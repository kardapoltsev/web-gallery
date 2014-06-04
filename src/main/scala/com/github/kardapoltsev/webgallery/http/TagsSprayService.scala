package com.github.kardapoltsev.webgallery.http

import spray.routing.{Route, HttpService}
import scala.concurrent.{Future, ExecutionContext}
import akka.util.Timeout
import spray.http._
import spray.json._
import com.github.kardapoltsev.webgallery.db.Tag



/**
 * Created by alexey on 6/4/14.
 */
trait TagsSprayService { this: HttpService =>
  implicit def executionContext: ExecutionContext
  implicit def requestTimeout: Timeout

  protected def getTags: Future[Seq[Tag]]

  val tagsRoute: Route = respondWithMediaType(MediaTypes.`application/json`){
    path("tags") {
       get { ctx =>
        getTags map {
          case tags => ctx.complete(tags.toJson.compactPrint)
        }
      }
    }
  }
}
