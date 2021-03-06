package com.github.kardapoltsev.webgallery

import akka.actor.{ Props, ActorLogging, Actor }
import akka.event.LoggingReceive
import com.github.kardapoltsev.webgallery.acl.Permissions
import com.github.kardapoltsev.webgallery.db.{ ImageInfo, Tag, ImageId, Image, Alternative }
import com.github.kardapoltsev.webgallery.es.{ ImageUntagged, ImageTagged, EventPublisher }
import com.github.kardapoltsev.webgallery.http._
import com.github.kardapoltsev.webgallery.processing.OptionalSize
import com.github.kardapoltsev.webgallery.routing.ImageHolderRequest
import com.github.kardapoltsev.webgallery.util.{ FilesUtil }
import scalikejdbc.{ DBSession, DB }
import spray.json.DefaultJsonProtocol

import scala.util.control.NonFatal

/**
 * Created by alexey on 8/30/14.
 */
object ImageHolder extends DefaultJsonProtocol {
  case class LikeImage(imageId: ImageId) extends AuthorizedRequest with ImageHolderRequest
  case class UnlikeImage(imageId: ImageId) extends AuthorizedRequest with ImageHolderRequest

  case class GetImage(imageId: Int)
      extends PrivilegedImageRequest with ImageHolderRequest {
    def permissions = Permissions.Read
  }

  case class TransformImageRequest(imageId: Int, size: OptionalSize)
    extends ApiRequest with ImageHolderRequest
  case class TransformImageResponse(alternative: Alternative) extends ApiResponse

  case class UpdateImageParams(tags: Option[Seq[Tag]])
  object UpdateImageParams {
    implicit val _ = jsonFormat1(UpdateImageParams.apply)
  }
  case class UpdateImage(imageId: Int, params: UpdateImageParams)
      extends PrivilegedImageRequest with ImageHolderRequest {
    def permissions = Permissions.Write
  }

  case class GetImageResponse(image: ImageInfo) extends ApiResponse
  object GetImageResponse {
    implicit val _ = jsonFormat1(GetImageResponse.apply)
  }

  def props(image: Image) = Props(new ImageHolder(image))
}

class ImageHolder(image: Image) extends Actor with ActorLogging with EventPublisher {
  import com.github.kardapoltsev.webgallery.db._
  import com.github.kardapoltsev.webgallery.http.marshalling._
  import com.github.kardapoltsev.webgallery.processing.Java2DImageImplicits._
  import ImageHolder._
  import context.dispatcher
  import Configs.AlternativesDir
  def tags = Tag.findByImageId(image.id)
  val owner = User.find(image.ownerId).get
  var likesCount = Like.countByImage(image.id)

  def receive: Receive = LoggingReceive(
    Seq(processGetImage, processUpdateImage, processLikeRequest, processUnlikeRequest, processTransformImage)
      reduceLeft (_ orElse _)
  )

  private def processTransformImage: Receive = {
    case r @ TransformImageRequest(imageId, size) =>
      sender() ! TransformImageResponse(findOrCreateAlternative(imageId, size))
  }

  private def findOrCreateAlternative(imageId: ImageId, size: OptionalSize): Alternative = {
    DB localTx { implicit s =>
      Alternative.find(imageId, size) match {
        case Some(alt) if alternativeExists(alt) =>
          if (alt.size == size) {
            log.debug(s"found existing $alt")
            alt
          } else {
            log.debug(s"alternative not found, creating new for $image with $size from $alt")
            createAlternative(imageId, Configs.AlternativesDir + alt.filename, size)
          }
        case Some(alt) =>
          log.debug(s"alternative found but not exists, create new one")
          Alternative.destroy(alt)
          createAlternative(image.id, Configs.OriginalsDir + image.filename, size)
        case None =>
          log.debug(s"alternative not found, creating new for $image with $size")
          createAlternative(image.id, Configs.OriginalsDir + image.filename, size)
      }
    }
  }

  private def alternativeExists(alt: Alternative): Boolean = {
    FilesUtil.exists(AlternativesDir + alt.filename)
  }

  def processGetImage: Receive = {
    case r: GetImage =>
      sender() ! GetImageResponse(this.toInfo(r.session.get.userId))
  }

  private def toInfo(requester: UserId): ImageInfo = {
    DB readOnly { implicit s =>
      val isLiked = Like.isLiked(image.id, requester)
      ImageInfo(
        image.id,
        image.name,
        image.filename,
        owner,
        image.ownerId,
        tags,
        likesCount = likesCount,
        isLiked = isLiked
      )
    }
  }

  def processUpdateImage: Receive = {
    case r: UpdateImage =>
      r.params.tags.foreach { t =>
        val newTags = t filter (_.ownerId == owner.id)
        val added = newTags filterNot (t => tags.exists(_.id == t.id))
        val deleted = tags filterNot (t => newTags.exists(_.id == t.id))
        DB localTx { implicit s =>
          added foreach { tag =>
            try {
              ImageTag.create(r.imageId, tag.id)
              publish(ImageTagged(image, tag))
            } catch {
              case NonFatal(e) => log.debug("error tagging image", e)
            }
          }
          deleted foreach { tag =>
            try {
              ImageTag.delete(r.imageId, tag.id)
              publish(ImageUntagged(image, tag))
            } catch {
              case NonFatal(e) => log.debug("error untagging image", e)
            }
          }
        }
      }
      sender() ! SuccessResponse
  }

  private def createAlternative(
    imageId: ImageId, path: String, size: OptionalSize)(implicit s: DBSession): Alternative = {
    val alt = imageFrom(path) scaledTo size
    val altFilename = FilesUtil.newFilename(path)
    alt.writeTo(Configs.AlternativesDir + altFilename)
    Alternative.create(imageId, altFilename, size)
  }

  private def processLikeRequest: Receive = {
    case r @ LikeImage(imageId) =>
      try {
        Like.create(imageId, r.session.get.userId)
        likesCount += 1
        sender() ! SuccessResponse
      } catch {
        case e: Exception => sender() ! ErrorResponse.UnprocessableEntity
      }
  }

  private def processUnlikeRequest: Receive = {
    case r @ UnlikeImage(imageId) =>
      Like.delete(imageId, r.session.get.userId)
      likesCount -= 1
      sender() ! SuccessResponse
  }

}
