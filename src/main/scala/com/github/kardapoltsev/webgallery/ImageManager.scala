package com.github.kardapoltsev.webgallery

import akka.actor.{ActorLogging, Actor}
import java.io.{FileOutputStream, File}
import com.github.kardapoltsev.webgallery.db._
import com.github.kardapoltsev.webgallery.http._
import com.github.kardapoltsev.webgallery.util.{FilesUtil, MetadataExtractor}
import spray.json.DefaultJsonProtocol
import com.github.kardapoltsev.webgallery.processing.{OptionalSize}
import org.joda.time.format.DateTimeFormat
import com.github.kardapoltsev.webgallery.routing.ImageProcessorRequest


/**
 * Created by alexey on 5/27/14.
 */
class ImageManager extends Actor with ActorLogging {
  import ImageManager._


  def receive: Receive = processTransformImage orElse {
    case r @ UploadImageRequest(filename, content) =>
      val img = saveFile(filename, content)
      sender() ! process(r.session.get.userId, img)
  }


  private def processTransformImage: Receive = {
    case msg @ TransformImageRequest(imageId, size) =>
      context.child(imageActorName(imageId)) match {
        case Some(imageHolder) => imageHolder forward msg
        case None =>
          Image.find(imageId) match {
            case Some(image) =>
              val imageHolder = context.actorOf(ImageHolder.props(image), imageActorName(imageId))
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


  private def createTag(ownerId: UserId, name: String): Tag = {
    Tag.find(ownerId, name.toLowerCase) match {
      case Some(t) => t
      case None => Tag.create(ownerId, name.toLowerCase)
    }
  }


  private def addTags(imageId: Int, tags: Seq[TagId]): Unit = {
    tags.foreach { id =>
      ImageTag.create(imageId, id)
    }
  }

}


object ImageManager extends DefaultJsonProtocol {
  case class TransformImageRequest(imageId: Int, size: OptionalSize) extends ImageProcessorRequest with ApiRequest
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
