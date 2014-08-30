package com.github.kardapoltsev.webgallery


import akka.actor.{Props, ActorLogging, Actor}
import akka.util.Timeout
import com.github.kardapoltsev.webgallery.ImageManager.{UploadImageRequest, TransformImageResponse, TransformImageRequest}
import com.github.kardapoltsev.webgallery.db.{Alternative, UserId, ImageMetadata, Image}
import com.github.kardapoltsev.webgallery.processing.OptionalSize
import com.github.kardapoltsev.webgallery.util.{MetadataExtractor, FilesUtil}
import org.joda.time.format.DateTimeFormat



/**
 * Created by alexey on 8/30/14.
 */
class ImageHolder(image: Image) extends Actor with ActorLogging {
  import com.github.kardapoltsev.webgallery.db._
  import com.github.kardapoltsev.webgallery.processing.Java2DImageImplicits._


  override def preStart(): Unit = {

  }


  def receive: Receive = {
    case TransformImageRequest(imageId, size) =>
      sender() ! TransformImageResponse(findOrCreateAlternative(imageId, size))
  }


  private def findOrCreateAlternative(imageId: Int, size: OptionalSize): Alternative = {
    import concurrent.duration._
    implicit val timeout = Timeout(10 seconds)

    Alternative.find(imageId, size) match {
      case Some(alt) =>
        if(alt.size == size){
          log.debug(s"found existing $alt")
          alt
        }
        else {
          log.debug(s"alternative not found, creating new for $image with $size")
          createAlternative(imageId, Configs.AlternativesDir + alt.filename, size)
        }
      case None =>
        createAlternative(image.id, Configs.OriginalsDir + image.filename, size)
    }
  }


  private def createAlternative(imageId: Int, path: String, size: OptionalSize): Alternative = {
    val alt = imageFrom(path) scaledTo size
    val altFilename = FilesUtil.newFilename(path)
    alt.writeTo(Configs.AlternativesDir + altFilename)
    Alternative.create(imageId, altFilename, size)
  }

}


object ImageHolder {
  def props(image: Image) = Props(new ImageHolder(image))
}
