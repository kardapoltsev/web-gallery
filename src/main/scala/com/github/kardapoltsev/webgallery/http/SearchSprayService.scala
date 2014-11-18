package com.github.kardapoltsev.webgallery.http

import com.github.kardapoltsev.webgallery.UserManager.{ SearchUsersResponse, SearchUsers }
import com.github.kardapoltsev.webgallery.tags.TagsManager
import spray.routing.{ Route, HttpService }
import scala.concurrent.{ Future, ExecutionContext }
import akka.util.Timeout
import TagsManager.{ SearchTags, GetTagsResponse }
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

  protected def searchTags(r: SearchTags) = processRequest(r)
  protected def searchUsers(r: SearchUsers) = processRequest(r)

  val searchRoute: Route =
    pathPrefix("api" / "search") {
      (path("tags") & parameters('term) & offsetLimit) { (query, offset, limit) =>
        dynamic {
          handleRequest(query :: offset :: limit :: HNil) {
            searchTags
          }
        }
      } ~
        (path("users") & parameters('term) & offsetLimit) { (query, offset, limit) =>
          dynamic {
            handleRequest(query :: offset :: limit :: HNil) {
              searchUsers
            }
          }
        }
    }
}
