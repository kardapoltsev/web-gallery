package com.github.kardapoltsev.webgallery.http


import spray.routing.{Route, HttpService}
import scala.concurrent.{Future, ExecutionContext}
import akka.util.Timeout
import spray.http._
import spray.json._
import com.github.kardapoltsev.webgallery.Database.GetTagsResponse



/**
 * Created by alexey on 6/4/14.
 */
trait SearchSprayService { this: HttpService =>
  import marshalling._
  import spray.httpx.marshalling._
  import spray.httpx.SprayJsonSupport._
  import BaseSprayService._

  implicit def executionContext: ExecutionContext
  implicit def requestTimeout: Timeout

  protected def searchTags(query: String): Result[GetTagsResponse]

  //TODO: SearchTags unmarshaller

  val searchRoute: Route =
    pathPrefix("search") {
      (path("tags") & parameters('term)) { query =>
        dynamic {
          handleWith {
            searchTags
          }
        }
      }
    }
}
