package com.github.kardapoltsev.webgallery.http


import com.github.kardapoltsev.webgallery.CommentManager.{GetCommentsResponse, GetComments, AddCommentResponse, AddComment}
import spray.routing.{Route, HttpService}
import scala.concurrent.{Future, ExecutionContext}
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

  protected def addComment(r: AddComment): Result[AddCommentResponse] = processRequest(r)
  protected def getComments(r: GetComments): Result[GetCommentsResponse] = processRequest(r)


  val commentRoute: Route =
    pathPrefix("api" / "images") {
      path(IntNumber / "comments") { imageId =>
        post {
          dynamic {
            handleWith(imageId :: HNil) {
              addComment
            }
          }
        } ~
        (get & offsetLimit) { (offset, limit) =>
          handleWith(imageId :: offset :: limit :: HNil){
            getComments
          }
        }
      }
    }

}
