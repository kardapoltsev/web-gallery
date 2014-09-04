package com.github.kardapoltsev.webgallery


import akka.actor.{Props, ActorLogging, Actor}
import akka.event.LoggingReceive
import akka.util.Timeout
import com.github.kardapoltsev.webgallery.ImageManager._
import com.github.kardapoltsev.webgallery.db.{ImageId, Image}
import com.github.kardapoltsev.webgallery.dto.ImageInfo
import com.github.kardapoltsev.webgallery.http.{SuccessResponse, ErrorResponse}
import com.github.kardapoltsev.webgallery.processing.OptionalSize
import com.github.kardapoltsev.webgallery.util.{FilesUtil}

import scala.util.control.NonFatal



/**
 * Created by alexey on 8/30/14.
 */
class ImageHolder(image: Image) extends Actor with ActorLogging {
  import com.github.kardapoltsev.webgallery.db._
  import com.github.kardapoltsev.webgallery.processing.Java2DImageImplicits._
  var tags = Tag.findByImageId(image.id)


  def receive: Receive = processGetImage orElse processUpdateImage orElse {
    case TransformImageRequest(imageId, size) =>
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


  private def processGetImage: Receive = {
    case GetImage(imageId) =>
      sender() ! GetImageResponse(ImageInfo(image, tags))
  }


  private def processUpdateImage: Receive = {
    case r: UpdateImage =>
      //TODO: insert only new tags and delete other tags
      r.params.tags.foreach { tags =>
        tags foreach { tag =>
          try {
            ImageTag.create(r.imageId, tag.id)
          } catch {
            case NonFatal(e) =>
          }
        }
        this.tags = tags
      }
      sender() ! SuccessResponse
  }


  private def createAlternative(imageId: ImageId, path: String, size: OptionalSize): Alternative = {
    val alt = imageFrom(path) scaledTo size
    val altFilename = FilesUtil.newFilename(path)
    alt.writeTo(Configs.AlternativesDir + altFilename)
    Alternative.create(imageId, altFilename, size)
  }

}

trait ImageHolderRequest {
  def imageId: ImageId
}
object ImageHolder {
  def props(image: Image) = Props(new ImageHolder(image))
}
