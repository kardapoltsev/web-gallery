package com.github.kardapoltsev.webgallery


import akka.actor.{ActorLogging, Actor}
import akka.event.LoggingReceive
import com.github.kardapoltsev.webgallery.db.ImageId
import com.github.kardapoltsev.webgallery.http.{ErrorResponse, SuccessResponse, AuthorizedRequest}
import com.github.kardapoltsev.webgallery.routing.LikeManagerRequest
import com.github.kardapoltsev.webgallery.db._



/**
 * Created by alexey on 8/28/14.
 */
class LikeManager extends Actor with ActorLogging {
  import LikeManager._

  def receive: Receive = LoggingReceive(processLikeRequest orElse processUnlikeRequest)

  private def processLikeRequest: Receive = {
    case r @ LikeImage(imageId) =>
      val response = try {
        Like.create(imageId, r.session.get.userId)
        SuccessResponse
      } catch {
        case e: Exception => ErrorResponse.UnprocessableEntity
      }
      sender() ! response
  }


  private def processUnlikeRequest: Receive = {
    case r @ UnlikeImage(imageId) =>
      Like.delete(imageId, r.session.get.userId)
      sender() ! SuccessResponse
  }

}


object LikeManager {
  case class LikeImage(imageId: ImageId) extends AuthorizedRequest with LikeManagerRequest
  case class UnlikeImage(imageId: ImageId) extends AuthorizedRequest with LikeManagerRequest
}
