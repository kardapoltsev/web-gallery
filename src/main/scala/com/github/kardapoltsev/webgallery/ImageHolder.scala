package com.github.kardapoltsev.webgallery


import akka.actor.{Props, ActorLogging, Actor}
import akka.event.LoggingReceive
import akka.util.Timeout
import com.github.kardapoltsev.webgallery.ImageManager.{TransformImageResponse, TransformImageRequest}
import com.github.kardapoltsev.webgallery.db.{Image}
import com.github.kardapoltsev.webgallery.processing.OptionalSize
import com.github.kardapoltsev.webgallery.util.{FilesUtil}



/**
 * Created by alexey on 8/30/14.
 */
class ImageHolder(image: Image) extends Actor with ActorLogging {
  import com.github.kardapoltsev.webgallery.db._
  import com.github.kardapoltsev.webgallery.processing.Java2DImageImplicits._


  def receive: Receive = LoggingReceive {
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


  private def createAlternative(imageId: ImageId, path: String, size: OptionalSize): Alternative = {
    val alt = imageFrom(path) scaledTo size
    val altFilename = FilesUtil.newFilename(path)
    alt.writeTo(Configs.AlternativesDir + altFilename)
    Alternative.create(imageId, altFilename, size)
  }

}


object ImageHolder {
  def props(image: Image) = Props(new ImageHolder(image))
}
