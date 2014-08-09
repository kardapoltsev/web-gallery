package com.github.kardapoltsev.webgallery.http


import spray.routing.{Route, HttpService}
import scala.concurrent.{Future, ExecutionContext}
import akka.util.Timeout
import com.github.kardapoltsev.webgallery.Database.{SearchTags, GetTagsResponse}
import shapeless._



/**
 * Created by alexey on 6/4/14.
 */
trait SearchSprayService extends BaseSprayService { this: HttpService =>
  import marshalling._
  import spray.httpx.marshalling._
  import BaseSprayService._

  implicit def executionContext: ExecutionContext
  implicit def requestTimeout: Timeout

  protected def searchTags(r: SearchTags): Result[GetTagsResponse] = processRequest(r)


  val searchRoute: Route =
    pathPrefix("api") {
      pathPrefix("search") {
        (path("tags") & parameters('term)) { query =>
          dynamic {
            handleWith(query :: HNil) {
              searchTags
            }
          }
        }
      }
    }
}
