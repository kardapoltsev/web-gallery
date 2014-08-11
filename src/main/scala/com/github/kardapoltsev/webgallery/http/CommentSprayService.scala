package com.github.kardapoltsev.webgallery.http


import com.github.kardapoltsev.webgallery.CommentManager.{AddCommentResponse, AddComment}
import com.github.kardapoltsev.webgallery.UserManager.{SearchUsersResponse, SearchUsers}
import spray.routing.{Route, HttpService}
import scala.concurrent.{Future, ExecutionContext}
import akka.util.Timeout
import com.github.kardapoltsev.webgallery.Database.{SearchTags, GetTagsResponse}
import shapeless._



/**
 * Created by alexey on 6/4/14.
 */
trait CommentSprayService extends BaseSprayService { this: HttpService =>
  import marshalling._
  import spray.httpx.marshalling._
  import BaseSprayService._

  implicit def executionContext: ExecutionContext
  implicit def requestTimeout: Timeout

  protected def addComment(r: AddComment): Result[AddCommentResponse] = processRequest(r)


  val commentRoute: Route =
    pathPrefix("api" / "images") {
      path(IntNumber / "comments") { imageId =>
        post {
          dynamic {
            handleWith(imageId :: HNil) {
              addComment
            }
          }
        }
      }
    }

}
