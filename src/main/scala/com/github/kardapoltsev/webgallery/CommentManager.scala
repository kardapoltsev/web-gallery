package com.github.kardapoltsev.webgallery


import akka.actor.{ActorLogging, Actor}
import com.github.kardapoltsev.webgallery.db._
import com.github.kardapoltsev.webgallery.http.{Pagination, AuthorizedRequest}
import com.github.kardapoltsev.webgallery.routing.CommentManagerRequest
import org.joda.time.{DateTime, DateTimeZone}
import spray.json.DefaultJsonProtocol

import scala.collection.mutable



/**
 * Created by alexey on 8/10/14.
 */
class CommentManager extends Actor with ActorLogging {
  import CommentManager._


  def receive = {
    case r @ AddComment(imageId, text, parentCommentId) =>
      val comment = Comment.create(imageId, parentCommentId, text, DateTime.now(DateTimeZone.UTC), r.session.get.userId)
      sender() ! AddCommentResponse(comment)
    case r @ GetComments(imageId) =>
      sender() ! getComments(imageId, r.offset, r.limit)
  }


  private def getComments(imageId: ImageId, offset: Int, limit: Int): GetCommentsResponse = {
    val comments = Comment.findByImageId(imageId, offset, limit) map { c =>
      CommentDto(c.id, c.imageId, c.parentCommentId, c.text, c.createTime, c.ownerId)
    }
    var map = comments.map { c => c.id -> c }.toMap
    var topLevel = Set[CommentId]()
    comments.foreach { c =>
      c.parentCommentId match {
        case Some(parentId) =>
          map.get(parentId) match {
            case Some(parent) => map += (parentId -> parent.copy(replies = parent.replies :+ c))
            case None => topLevel += c.id
          }
        case None => topLevel += c.id
      }
    }
    GetCommentsResponse(map.values.toSeq.filter(c => topLevel(c.id)))
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
  case class GetCommentsResponse(comments: Seq[CommentDto])
  object GetCommentsResponse {
    implicit val _ = jsonFormat1(GetCommentsResponse.apply)
  }

}
