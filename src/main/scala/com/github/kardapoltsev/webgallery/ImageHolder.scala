package com.github.kardapoltsev.webgallery


import akka.actor.{Props, ActorLogging, Actor}
import com.github.kardapoltsev.webgallery.acl.Permissions
import com.github.kardapoltsev.webgallery.db.{ImageInfo, Tag, ImageId, Image, Alternative}
import com.github.kardapoltsev.webgallery.es.{ImageUntagged, ImageTagged, EventPublisher}
import com.github.kardapoltsev.webgallery.http.{AuthorizedRequest, ApiRequest, SuccessResponse, ErrorResponse}
import com.github.kardapoltsev.webgallery.processing.OptionalSize
import com.github.kardapoltsev.webgallery.routing.ImageHolderRequest
import com.github.kardapoltsev.webgallery.util.{FilesUtil}
import spray.json.DefaultJsonProtocol

import scala.util.control.NonFatal



/**
 * Created by alexey on 8/30/14.
 */
class ImageHolder(image: Image) extends Actor with ActorLogging with EventPublisher {
  import com.github.kardapoltsev.webgallery.db._
  import com.github.kardapoltsev.webgallery.processing.Java2DImageImplicits._
  import ImageHolder._
  def tags = Tag.findByImageId(image.id)
  val owner = User.find(image.ownerId).get
  var likesCount = Like.countByImage(image.id)


  def receive: Receive = processGetImage orElse processUpdateImage orElse processLikeRequest orElse
      processUnlikeRequest orElse {
    case r @ TransformImageRequest(imageId, size) =>
      sender() ! TransformImageResponse(findOrCreateAlternative(imageId, size))
  }


  private def findOrCreateAlternative(imageId: ImageId, size: OptionalSize): Alternative = {
    Alternative.find(imageId, size) match {
      case Some(alt) =>
        if(alt.size == size){
          log.debug(s"found existing $alt")
          alt
        }
        else {
          log.debug(s"alternative not found, creating new for $image with $size from $alt")
          createAlternative(imageId, Configs.AlternativesDir + alt.filename, size)
        }
      case None =>
        log.debug(s"alternative not found, creating new for $image with $size")
        createAlternative(image.id, Configs.OriginalsDir + image.filename, size)
    }
  }


  def processGetImage: Receive = {
    case r: GetImage =>
      sender() ! GetImageResponse(this.toInfo(r.session.get.userId))
  }


  private def toInfo(requester: UserId): ImageInfo = {
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


  def processUpdateImage: Receive = {
    case r: UpdateImage =>
      //TODO: check tags ownership
      r.params.tags.foreach { t =>
        val newTags = t filter(_.ownerId == owner.id)
        val added = newTags filterNot (t => tags.exists(_.id == t.id))
        val deleted = tags filterNot (t => newTags.exists(_.id == t.id))
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
      sender() ! SuccessResponse
  }


  private def createAlternative(imageId: ImageId, path: String, size: OptionalSize): Alternative = {
    val alt = imageFrom(path) scaledTo size
    val altFilename = FilesUtil.newFilename(path)
    alt.writeTo(Configs.AlternativesDir + altFilename)
    Alternative.create(imageId, altFilename, size)
  }


  private def processLikeRequest: Receive = {
    case r @ LikeImage(imageId) =>
      val response = try {
        Like.create(imageId, r.session.get.userId)
        likesCount += 1
        SuccessResponse
      } catch {
        case e: Exception => ErrorResponse.UnprocessableEntity
      }
      sender() ! response
  }


  private def processUnlikeRequest: Receive = {
    case r @ UnlikeImage(imageId) =>
      Like.delete(imageId, r.session.get.userId)
      likesCount -= 1
      sender() ! SuccessResponse
  }

}




object ImageHolder extends DefaultJsonProtocol {
  case class LikeImage(imageId: ImageId) extends AuthorizedRequest with ImageHolderRequest
  case class UnlikeImage(imageId: ImageId) extends AuthorizedRequest with ImageHolderRequest

  case class GetImage(imageId: Int)
      extends PrivilegedImageRequest with ImageHolderRequest {
    def permissions = Permissions.Read
  }


  case class TransformImageRequest(imageId: Int, size: OptionalSize)
      extends ApiRequest with ImageHolderRequest
  case class TransformImageResponse(alternative: Alternative)


  case class UpdateImageParams(tags: Option[Seq[Tag]])
  object UpdateImageParams {
    implicit val _ = jsonFormat1(UpdateImageParams.apply)
  }
  case class UpdateImage(imageId: Int, params: UpdateImageParams)
      extends PrivilegedImageRequest with ImageHolderRequest {
    def permissions = Permissions.Write
  }
  object UpdateImage {
    implicit val _ = jsonFormat2(UpdateImage.apply)
  }


  case class GetImageResponse(image: ImageInfo)
  object GetImageResponse {
    implicit val _ = jsonFormat1(GetImageResponse.apply)
  }


  def props(image: Image) = Props(new ImageHolder(image))
}
