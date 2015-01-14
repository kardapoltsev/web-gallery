package com.github.kardapoltsev.webgallery

import akka.actor.{ ActorLogging, Actor }
import akka.event.LoggingReceive
import com.github.kardapoltsev.webgallery.acl.Permissions
import com.github.kardapoltsev.webgallery.acl.Permissions.Permission
import com.github.kardapoltsev.webgallery.db.EntityType.EntityType
import com.github.kardapoltsev.webgallery.db._
import com.github.kardapoltsev.webgallery.http._
import com.github.kardapoltsev.webgallery.routing.CommentManagerRequest
import org.joda.time.{ DateTime, DateTimeZone }
import spray.json.DefaultJsonProtocol

import scalikejdbc.DB

/**
 * Created by alexey on 8/10/14.
 */
object CommentManager extends DefaultJsonProtocol {

  case class AddComment(imageId: ImageId, text: String, parentCommentId: Option[CommentId])
    extends AuthorizedRequest with CommentManagerRequest
  case class AddCommentResponse(comment: Comment) extends ApiResponse
  object AddCommentResponse {
    implicit val _ = jsonFormat1(AddCommentResponse.apply)
  }

  case class GetComments(imageId: ImageId) extends AuthorizedRequest with CommentManagerRequest with Pagination
  case class GetCommentsResponse(comments: Seq[CommentInfo]) extends ApiResponse
  object GetCommentsResponse {
    implicit val _ = jsonFormat1(GetCommentsResponse.apply)
  }

  case class DeleteComment(commentId: CommentId)
      extends PrivilegedRequest with CommentManagerRequest {
    override def permissions: Permission = Permissions.Write
    override def subjectId: Int = commentId
    override def subjectType: EntityType = EntityType.Comment
  }

}

class CommentManager extends Actor with ActorLogging {
  import CommentManager._
  import context.dispatcher
  import com.github.kardapoltsev.webgallery.http.marshalling._

  def receive = LoggingReceive(
    Seq(processAddComment, processGetComments, deleteComment) reduceLeft (_ orElse _)
  )

  private def processAddComment: Receive = {
    case r @ AddComment(imageId, text, parentCommentId) =>
      val comment = DB.localTx { implicit s =>
        val comment = Comment.create(imageId, parentCommentId, text, DateTime.now(DateTimeZone.UTC), r.session.get.userId)
        if (comment.parentCommentId.isEmpty)
          Comment.save(comment.copy(parentCommentId = Some(comment.id)))
        else comment
      }
      r.complete(AddCommentResponse(comment))
  }

  private def processGetComments: Receive = {
    case r @ GetComments(imageId) =>
      val comments = DB readOnly { implicit s =>
        CommentInfo.findByImageId(imageId, r.offset, r.limit)
      }
      r.complete(GetCommentsResponse(comments))
  }

  private def deleteComment: Receive = {
    case r @ DeleteComment(commentId) =>
      Comment.find(commentId) match {
        case Some(comment) =>
          Comment.destroy(comment)
          r.complete(SuccessResponse)
        case None =>
          r.complete(ErrorResponse.NotFound)
      }
  }

}
