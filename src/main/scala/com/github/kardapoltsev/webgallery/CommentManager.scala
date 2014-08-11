package com.github.kardapoltsev.webgallery


import akka.actor.{ActorLogging, Actor}
import com.github.kardapoltsev.webgallery.db._
import com.github.kardapoltsev.webgallery.http.AuthorizedRequest
import com.github.kardapoltsev.webgallery.routing.CommentManagerRequest
import org.joda.time.{DateTime, DateTimeZone}
import spray.json.DefaultJsonProtocol



/**
 * Created by alexey on 8/10/14.
 */
class CommentManager extends Actor with ActorLogging {
  import CommentManager._


  def receive = {
    case r @ AddComment(imageId, text, parentCommentId) =>
      val comment = Comment.create(imageId, parentCommentId, text, DateTime.now(DateTimeZone.UTC), r.session.get.userId)
      sender() ! AddCommentResponse(comment)
  }
}


object CommentManager extends DefaultJsonProtocol {
  case class AddComment(imageId: ImageId, text: String, parentCommentId: Option[CommentId])
      extends AuthorizedRequest with CommentManagerRequest
  case class AddCommentResponse(comment: Comment)
  object AddCommentResponse {
    implicit val _ = jsonFormat1(AddCommentResponse.apply)
  }
}
