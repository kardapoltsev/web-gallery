package com.github.kardapoltsev.webgallery

import akka.actor.{ActorLogging, Actor}
import java.io.{FileOutputStream, File}
import com.github.kardapoltsev.webgallery.acl.Permissions
import com.github.kardapoltsev.webgallery.db._
import com.github.kardapoltsev.webgallery.dto.ImageInfo
import com.github.kardapoltsev.webgallery.http._
import com.github.kardapoltsev.webgallery.util.{FilesUtil, MetadataExtractor}
import scalikejdbc.DB
import spray.json.DefaultJsonProtocol
import com.github.kardapoltsev.webgallery.processing.{OptionalSize}
import org.joda.time.format.DateTimeFormat
import com.github.kardapoltsev.webgallery.routing.ImageProcessorRequest

import scala.util.control.NonFatal



/**
 * Created by alexey on 5/27/14.
 */
class ImageManager extends Actor with ActorLogging {
  import ImageManager._


  def receive: Receive = processGetImagesByTag orElse processUploadImage orElse {
    case msg: GetImage => forwardToHolder(msg)
    case msg: TransformImageRequest => forwardToHolder(msg)
    case msg: UpdateImage => forwardToHolder(msg)
  }


  private def processUploadImage: Receive = {
    case r @ UploadImageRequest(filename, content) =>
      val img = saveFile(filename, content)
      sender() ! process(r.session.get.userId, img)
  }


  private def processGetImagesByTag: Receive = {
    case r @ GetByTag(tagId) =>
    val userId = r.session.get.userId
    log.debug(s"searching by tagId $tagId for userId $userId")
    val images = Image.findByTag(tagId, userId) map { image =>
      val tags = Tag.findByImageId(image.id)
      ImageInfo(image, tags)
    }
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


  private def process(ownerId: UserId, file: File): ApiResponse = {
    val filename = FilesUtil.newFilename(file.getName)
    val image = Image.create(file.getName, filename, ownerId)
    MetadataExtractor.process(file) foreach { meta =>
      log.debug(s"extracted meta for $image: $meta")
      val tags = extractTags(meta)
      val tagIds = tags.map(t => createTag(ownerId, t)).map(_.id)
      addTags(image.id, tagIds)
      Metadata.create(image.id, meta.cameraModel, meta.creationTime)

    }
    FilesUtil.moveFile(file, Configs.OriginalsDir + filename)
    UploadImageResponse(image.id)
  }


  @deprecated("send message to TagsManager", since = "2014-09-05")
  private def createTag(ownerId: UserId, name: String): Tag = {
    Tag.find(ownerId, name.toLowerCase) match {
      case Some(t) => t
      case None =>
        DB.localTx { implicit s =>
          val t = Tag.create(ownerId, name.toLowerCase)
          Acl.create(t.id, ownerId)
          t
        }
    }
  }


  private def addTags(imageId: Int, tags: Seq[TagId]): Unit = {
    tags.foreach { id =>
      ImageTag.create(imageId, id)
    }
  }

}


trait PrivilegedImageRequest extends PrivilegedRequest {
  def imageId: ImageId
  def subjectType = EntityType.Image
  def subjectId = imageId
}


object ImageManager extends DefaultJsonProtocol {
  case class GetImage(imageId: Int)
      extends PrivilegedImageRequest with ImageProcessorRequest with ImageHolderRequest {
    def permissions = Permissions.Read
  }
  case class GetByTag(tagId: TagId) extends PrivilegedRequest with ImageProcessorRequest {
    def permissions = Permissions.Read
    def subjectType = EntityType.Tag
    def subjectId = tagId
  }
  case class GetImageResponse(image: ImageInfo)
  object GetImageResponse {
    implicit val _ = jsonFormat1(GetImageResponse.apply)
  }


  case class UpdateImageParams(tags: Option[Seq[Tag]])
  object UpdateImageParams {
    implicit val _ = jsonFormat1(UpdateImageParams.apply)
  }
  case class UpdateImage(imageId: Int, params: UpdateImageParams)
      extends PrivilegedImageRequest with ImageProcessorRequest with ImageHolderRequest {
    def permissions = Permissions.Write
  }
  object UpdateImage {
    implicit val _ = jsonFormat2(UpdateImage.apply)
  }


  case class GetImagesResponse(images: Seq[ImageInfo])
  object GetImagesResponse {
    implicit val _ = jsonFormat1(GetImagesResponse.apply)
  }


  case class TransformImageRequest(imageId: Int, size: OptionalSize)
      extends ImageProcessorRequest with ApiRequest with ImageHolderRequest
  case class TransformImageResponse(alternative: Alternative)

  case class UploadImageRequest(filename: String, content: Array[Byte])
    extends ImageProcessorRequest with AuthorizedRequest
  case class UploadImageResponse(imageId: ImageId) extends ApiResponse
  case object UploadImageResponse {
    implicit val _ = jsonFormat1(UploadImageResponse.apply)
  }


  def extractTags(m: ImageMetadata): Seq[String] = {
    m.keywords ++ Seq(
      m.cameraModel,
      m.creationTime.map(d => DateTimeFormat.forPattern("yyyy-MM-dd").print(d))
    ).flatten
  }.map(_.toLowerCase)

}
