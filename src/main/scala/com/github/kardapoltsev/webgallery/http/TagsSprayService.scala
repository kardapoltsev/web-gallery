package com.github.kardapoltsev.webgallery.http


import com.github.kardapoltsev.webgallery.tags.TagsManager
import spray.routing.{Route, HttpService}
import scala.concurrent.{ExecutionContext}
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

  protected def createTag(r: CreateTag): Result[CreateTagResponse] = processRequest(r)
  protected def getTags(r: GetTags): Result[GetTagsResponse] = processRequest(r)
  protected def getTag(r: GetTag): Result[GetTagResponse] = processRequest(r)
  protected def getRecentTags(r: GetRecentTags): Result[GetTagsResponse] = processRequest(r)
  protected def updateTag(r: UpdateTag): Result[SuccessResponse] = processRequest(r)

  val tagsRoute: Route = respondWithMediaType(MediaTypes.`application/json`) {
    pathPrefix("api") {
      pathPrefix("users" / IntNumber / "tags") { userId =>
        (path("recent") & offsetLimit & get) { (offset, limit) =>
          dynamic {
            handleWith(userId :: offset :: limit :: HNil) {
              getRecentTags
            }
          }
        } ~
        path(IntNumber) { tagId =>
          get {
            dynamic {
              handleWith(tagId :: HNil) {
                getTag
              }
            }
          } ~
          patch {
            dynamic {
              handleWith(tagId :: HNil) {
                updateTag
              }
            }
          }
        } ~
        (pathEnd & get) {
          dynamic {
            handleWith(userId :: HNil) {
              getTags
            }
          }
        } ~
        post {
          dynamic {
            handleWith {
              createTag
            }
          }
        }
      }
    }
  }
}
