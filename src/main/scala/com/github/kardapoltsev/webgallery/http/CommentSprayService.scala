package com.github.kardapoltsev.webgallery.http

import com.github.kardapoltsev.webgallery.CommentManager._
import spray.routing.{ Route, HttpService }
import scala.concurrent.{ Future, ExecutionContext }
import akka.util.Timeout
import shapeless._

/**
 * Created by alexey on 6/4/14.
 */
trait CommentSprayService extends BaseSprayService { this: HttpService =>
  import marshalling._
  import BaseSprayService._

  implicit def executionContext: ExecutionContext
  implicit def requestTimeout: Timeout

  protected def addComment(r: AddComment) = processRequest(r)
  protected def getComments(r: GetComments) = processRequest(r)
  protected def deleteComment(r: DeleteComment) = processRequest(r)

  val commentRoute: Route =
    pathPrefix("api" / "images") {
      pathPrefix(IntNumber / "comments") { imageId =>
        (pathEnd & post) {
          perRequest(imageId :: HNil) {
            r: AddComment => HandlerWrapper[AddCommentResponse](r)
          }
        } ~
        (pathEnd & get & offsetLimit) { (offset, limit) =>
          perRequest(imageId :: offset :: limit :: HNil) {
            r: GetComments => HandlerWrapper[GetCommentsResponse](r)
          }
        } ~
        (path(IntNumber) & delete) { commentId =>
          perRequest(commentId :: HNil) {
            r: DeleteComment => HandlerWrapper[SuccessResponse](r)
          }
        }
      }
    }
}
