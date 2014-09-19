package com.github.kardapoltsev.webgallery

import akka.actor.{ActorLogging, Actor}
import java.io.{FileOutputStream, File}
import com.github.kardapoltsev.webgallery.acl.Permissions
import com.github.kardapoltsev.webgallery.db._
import com.github.kardapoltsev.webgallery.http._
import com.github.kardapoltsev.webgallery.util.{FilesUtil, MetadataExtractor}
import scalikejdbc.DB
import spray.json.DefaultJsonProtocol
import org.joda.time.format.DateTimeFormat
import com.github.kardapoltsev.webgallery.routing.{ImageHolderRequest, ImageManagerRequest}



/**
 * Created by alexey on 5/27/14.
 */
class ImageManager extends Actor with ActorLogging {
  import ImageManager._


  def receive: Receive = processGetImagesByTag orElse processUploadImage orElse processGetPopularImages orElse {
    case msg: ImageHolderRequest => forwardToHolder(msg)
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
    val images = ImageInfo.findByTag(tagId, userId, r.offset, r.limit)
    sender() ! GetImagesResponse(images)
  }


  private def processGetPopularImages: Receive = {
    case r: GetPopularImages.type =>
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


  private def process(ownerId: UserId, file: File): ApiResponse = {
    val filename = FilesUtil.newFilename(file.getName)
    val image = Image.create(file.getName, filename, ownerId)
    MetadataExtractor.process(file) foreach { meta =>
      log.debug(s"extracted meta for $image: $meta")
      val tags = extractTags(meta)
      val tagIds = tags.map(t => createTag(ownerId, t, image.id)).map(_.id)
      addTags(image.id, tagIds)
      Metadata.create(image.id, meta.cameraModel, meta.creationTime)

    }
    FilesUtil.moveFile(file, Configs.OriginalsDir + filename)
    UploadImageResponse(image.id)
  }


  @deprecated("send message to TagsManager", since = "2014-09-05")
  private def createTag(ownerId: UserId, name: String, coverId: ImageId): Tag = {
    Tag.find(ownerId, name.toLowerCase) match {
      case Some(t) => t
      case None =>
        DB.localTx { implicit s =>
          val t = Tag.create(ownerId, name.toLowerCase, coverId)
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


  def extractTags(m: ImageMetadata): Seq[String] = {
    m.keywords ++ Seq(
      m.cameraModel,
      m.creationTime.map(d => DateTimeFormat.forPattern("yyyy-MM-dd").print(d))
    ).flatten
  }.map(_.toLowerCase)

}
