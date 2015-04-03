package com.github.kardapoltsev.webgallery.http

import com.github.kardapoltsev.webgallery.tags.TagsManager
import spray.routing.{ Route, HttpService }
import scala.concurrent.{ ExecutionContext }
import akka.util.Timeout
import spray.http._
import shapeless._
import TagsManager._

/**
 * Created by alexey on 6/4/14.
 */
trait TagsSprayService extends BaseSprayService { this: HttpService =>
  import marshalling._
  import BaseSprayService._

  implicit def executionContext: ExecutionContext
  implicit def requestTimeout: Timeout

  val tagsRoute: Route = respondWithMediaType(MediaTypes.`application/json`) {
    pathPrefix("api") {
      pathPrefix("users" / IntNumber / "tags") { userId =>
        (path("recent") & offsetLimit & get) { (offset, limit) =>
          perRequest(userId :: offset :: limit :: HNil) {
            createWrapper[GetRecentTags, GetTagsResponse]
          }
        } ~
          path(IntNumber) { tagId =>
            get {
              perRequest(tagId :: HNil) {
                createWrapper[GetTag, GetTagResponse]
              }
            } ~
              patch {
                perRequest(tagId :: HNil) {
                  createWrapper[UpdateTag, SuccessResponse]
                }
              }
          } ~
          (pathEnd & get) {
            perRequest(userId :: HNil) {
              createWrapper[GetTags, GetTagsResponse]
            }
          } ~
          post {
            perRequest[CreateTag, CreateTagResponse]
          }
      }
    }
  }

}
