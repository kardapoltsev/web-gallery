package com.github.kardapoltsev.webgallery

import akka.actor.{ActorLogging, Actor}
import java.io.{FileOutputStream, File}
import com.github.kardapoltsev.webgallery.acl.Permissions
import com.github.kardapoltsev.webgallery.db._
import com.github.kardapoltsev.webgallery.es.{ImageCreated, EventPublisher}
import com.github.kardapoltsev.webgallery.http._
import com.github.kardapoltsev.webgallery.util.{FilesUtil, MetadataExtractor}
import scalikejdbc.DB
import spray.json.DefaultJsonProtocol
import org.joda.time.format.DateTimeFormat
import com.github.kardapoltsev.webgallery.routing.{ImageHolderRequest, ImageManagerRequest}



/**
 * Created by alexey on 5/27/14.
 */
class ImageManager extends Actor with ActorLogging with EventPublisher {
  import ImageManager._


  def receive: Receive = processGetImagesByTag orElse processUploadImage orElse processGetPopularImages orElse {
    case msg: ImageHolderRequest => forwardToHolder(msg)
  }


  private def processUploadImage: Receive = {
    case r @ UploadImageRequest(filename, content) =>
      val img = saveFile(filename, content)
      val (image, meta) = process(r.session.get.userId, img)
      sender() ! UploadImageResponse(image.id)
      publish(ImageCreated(image, meta))
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


  private def forwardToHolder(msg: ImageHolderRequest): Unit = {
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
    val path = Configs.UnprocessedDir + "/" + filename
    val fos = new FileOutputStream(path)
    try {
      fos.write(content)
      new File(path)
    } finally {
      fos.close()
    }
  }


  private def process(ownerId: UserId, file: File): (Image, Option[ImageMetadata]) = {
    val filename = FilesUtil.newFilename(file.getName)
    val image = Image.create(file.getName, filename, ownerId)
    val meta = MetadataExtractor.process(file) map { meta =>
      log.debug(s"extracted meta for $image: $meta")
      Metadata.create(image.id, meta.cameraModel, meta.creationTime)
      meta
    }
    FilesUtil.moveFile(file, Configs.OriginalsDir + filename)
    image -> meta
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


  case object GetPopularImages extends AuthorizedRequest with Pagination with ImageManagerRequest


  case class GetImagesResponse(images: Seq[ImageInfo])
  object GetImagesResponse {
    implicit val _ = jsonFormat1(GetImagesResponse.apply)
  }


  case class UploadImageRequest(filename: String, content: Array[Byte])
    extends ImageManagerRequest with AuthorizedRequest
  case class UploadImageResponse(imageId: ImageId) extends ApiResponse
  case object UploadImageResponse {
    implicit val _ = jsonFormat1(UploadImageResponse.apply)
  }


}
