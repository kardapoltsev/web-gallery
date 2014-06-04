package com.github.kardapoltsev.webgallery.http


import spray.routing.{Route, HttpService}
import scala.concurrent.{Future, ExecutionContext}
import akka.util.Timeout
import spray.http._
import spray.json._



/**
 * Created by alexey on 6/4/14.
 */
trait SearchSprayService { this: HttpService =>
  implicit def executionContext: ExecutionContext
  implicit def requestTimeout: Timeout

  protected def searchTags(query: String): Future[Seq[String]]

  val searchRoute: Route =
    pathPrefix("search") {
      (path("tags") & parameters('term)) { query =>
        complete {
          searchTags(query) map {tags =>
            HttpResponse(StatusCodes.OK, HttpEntity(
              ContentTypes.`application/json`,
              JsArray(tags.map(t => JsString(t)).toList).compactPrint
              )
            )
          }
        }
      }
    }
}
