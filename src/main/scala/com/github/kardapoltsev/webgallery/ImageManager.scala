package com.github.kardapoltsev.webgallery

import akka.actor.{ActorLogging, Actor}
import java.io.{FileOutputStream, File}
import akka.event.LoggingReceive
import com.github.kardapoltsev.webgallery.UserManager.SetUserAvatar
import com.github.kardapoltsev.webgallery.acl.Permissions
import com.github.kardapoltsev.webgallery.db._
import com.github.kardapoltsev.webgallery.es.{ImageCreated, EventPublisher}
import com.github.kardapoltsev.webgallery.http._
import com.github.kardapoltsev.webgallery.util.{Hardcoded, FilesUtil, MetadataExtractor}
import scalikejdbc.DB
import spray.json.DefaultJsonProtocol
import org.joda.time.format.DateTimeFormat
import com.github.kardapoltsev.webgallery.routing.{ImageHolderRequest, ImageManagerRequest}



/**
 * Created by alexey on 5/27/14.
 */
class ImageManager extends Actor with ActorLogging with EventPublisher {
  import ImageManager._
  private val router = WebGalleryActorSelection.routerSelection


  def receive: Receive = LoggingReceive(
    Seq(processGetImagesByTag, processUploadImage, processUploadAvatar, processGetPopularImages, forwardToHolder).
      reduceLeft(_ orElse _)
  )

  
  private def processUploadImage: Receive = {
    case r @ UploadImage(filename, content) =>
      val img = saveFile(filename, content)
      val image = Image.create(filename, img.getName, r.session.get.userId)
      val meta = extractMetadata(img, image)
      sender() ! UploadImageResponse(image.id)
      publish(ImageCreated(image, meta))
  }


  private def processUploadAvatar: Receive = {
    case r @ UploadAvatar(filename, content) =>
      val img = saveFile(filename, content)
      val image = Image.create(filename, img.getName, r.session.get.userId)
      router ! SetUserAvatar(r.session.get.userId, image.id)
      sender() ! SuccessResponse
  }


  private def processGetImagesByTag: Receive = {
    case r @ GetByTag(tagId) =>
    val userId = r.session.get.userId
    log.debug(s"searching by tagId $tagId for userId $userId")
    val images = ImageInfo.findByTag(tagId, userId, r.offset, r.limit)
    sender() ! GetImagesResponse(images)
  }


  private def processGetPopularImages: Receive = {
    case r: GetPopularImages.type =>
      log.debug(s"searching popular images with offset ${r.offset}, limit ${r.limit}")
      val images = ImageInfo.findPopular(r.session.get.userId, r.offset, r.limit)
      sender() ! GetImagesResponse(images)
  }


  private def forwardToHolder: Receive = {
    case msg: ImageHolderRequest =>
      context.child(imageActorName(msg.imageId)) match {
        case Some(imageHolder) => imageHolder forward msg
        case None =>
          Image.find(msg.imageId) match {
            case Some(image) =>
              val imageHolder = context.actorOf(ImageHolder.props(image), imageActorName(msg.imageId))
              imageHolder forward msg
            case None => sender() ! ErrorResponse.NotFound
          }
      }
  }


  private def imageActorName(imageId: ImageId) = s"imageHolder-$imageId"


  private def saveFile(filename: String, content: Array[Byte]): File = {
    val fn = FilesUtil.newFilename(filename)
    val path = Configs.OriginalsDir + "/" + fn
    val fos = new FileOutputStream(path)
    try {
      fos.write(content)
      new File(path)
    } finally {
      fos.close()
    }
  }


  private def extractMetadata(file: File, image: Image): Option[ImageMetadata] = {
    MetadataExtractor.process(file) map { meta =>
      log.debug(s"extracted meta for $image: $meta")
      Metadata.create(image.id, meta.cameraModel, meta.creationTime)
      meta
    }
  }

}


trait PrivilegedImageRequest extends PrivilegedRequest {
  def imageId: ImageId
  def subjectType = EntityType.Image
  def subjectId = imageId
}


object ImageManager extends DefaultJsonProtocol {
  case class GetByTag(tagId: TagId) extends PrivilegedRequest with Pagination with ImageManagerRequest {
    def permissions = Permissions.Read
    def subjectType = EntityType.Tag
    def subjectId = tagId
  }


  case object GetPopularImages extends ApiRequest with Pagination with ImageManagerRequest


  case class GetImagesResponse(images: Seq[ImageInfo])
  object GetImagesResponse {
    implicit val _ = jsonFormat1(GetImagesResponse.apply)
  }


  case class UploadImage(filename: String, content: Array[Byte]) extends ImageManagerRequest with AuthorizedRequest
  case class UploadImageResponse(imageId: ImageId) extends ApiResponse
  case object UploadImageResponse {
    implicit val _ = jsonFormat1(UploadImageResponse.apply)
  }

  
  case class UploadAvatar(filename: String, content: Array[Byte]) extends ImageManagerRequest with AuthorizedRequest

}
