package com.github.kardapoltsev.webgallery


import akka.actor.{ActorLogging, Actor}
import com.github.kardapoltsev.webgallery.db._
import com.github.kardapoltsev.webgallery.http.{Pagination, AuthorizedRequest}
import com.github.kardapoltsev.webgallery.routing.CommentManagerRequest
import org.joda.time.{DateTime, DateTimeZone}
import spray.json.DefaultJsonProtocol

import scalikejdbc.DB



/**
 * Created by alexey on 8/10/14.
 */
class CommentManager extends Actor with ActorLogging {
  import CommentManager._


  def receive = {
    case r @ AddComment(imageId, text, parentCommentId) =>
      val comment = DB.localTx { implicit s =>
        val comment = Comment.create(imageId, parentCommentId, text, DateTime.now(DateTimeZone.UTC), r.session.get.userId)
        if(comment.parentCommentId.isEmpty)
          comment.copy(parentCommentId = Some(comment.id)).save()
        else comment
      }
      sender() ! AddCommentResponse(comment)
    case r @ GetComments(imageId) =>
      sender() ! getComments(imageId, r.offset, r.limit)
  }


  private def getComments(imageId: ImageId, offset: Int, limit: Int): GetCommentsResponse = {
    val comments = Comment.findByImageId(imageId, offset, limit)
    GetCommentsResponse(comments)
  }
}


object CommentManager extends DefaultJsonProtocol {

  case class AddComment(imageId: ImageId, text: String, parentCommentId: Option[CommentId])
      extends AuthorizedRequest with CommentManagerRequest
  case class AddCommentResponse(comment: Comment)
  object AddCommentResponse {
    implicit val _ = jsonFormat1(AddCommentResponse.apply)
  }


  case class GetComments(imageId: ImageId) extends AuthorizedRequest with CommentManagerRequest with Pagination
  case class GetCommentsResponse(comments: Seq[Comment])
  object GetCommentsResponse {
    implicit val _ = jsonFormat1(GetCommentsResponse.apply)
  }

}
