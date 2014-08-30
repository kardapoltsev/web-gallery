package com.github.kardapoltsev.webgallery

import akka.actor.{Props, ActorLogging, Actor}
import java.io.{FileOutputStream, File}
import com.github.kardapoltsev.webgallery.db._
import com.github.kardapoltsev.webgallery.http.ErrorResponse.InternalServerError
import com.github.kardapoltsev.webgallery.http._
import com.github.kardapoltsev.webgallery.util.{FilesUtil, MetadataExtractor}
import akka.pattern.{ask, pipe}
import com.github.kardapoltsev.webgallery.Database._
import akka.util.Timeout
import scala.concurrent.Future
import com.github.kardapoltsev.webgallery.Database.CreateImage
import com.github.kardapoltsev.webgallery.processing.{OptionalSize, SpecificSize}
import org.joda.time.format.DateTimeFormat
import scala.util.control.NonFatal
import com.github.kardapoltsev.webgallery.routing.ImageProcessorRequest


/**
 * Created by alexey on 5/27/14.
 */
class ImageProcessor extends Actor with ActorLogging {
  import concurrent.duration._
  import ImageProcessor._
  import context.dispatcher
  implicit val timeout = Timeout(1.second)

  private val router = WebGalleryActorSelection.routerSelection


  override def preStart(): Unit = {
  }

  def receive: Receive = processTransformImage orElse {
    case r @ UploadImageRequest(filename, content) =>
      val img = saveFile(filename, content)
      process(r.session.get.userId, img) pipeTo sender()
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



  private def process(ownerId: UserId, file: File): Future[ApiResponse] = {
    val meta = MetadataExtractor.process(file)
    val tags = extractTags(meta)
    val filename = FilesUtil.newFilename(file.getName)
    router ? CreateImage(ownerId, file.getName, filename, meta, tags) map {
      case CreateImageResponse(image) =>
        FilesUtil.moveFile(file, Configs.OriginalsDir + filename)
        log.debug(s"created image with $meta")
        SuccessResponse
    } recover {
      case NonFatal(e) =>
        log.error(e, s"couldn't process file $file")
        file.delete()
        InternalServerError
    }
  }

}


object ImageProcessor {
  case object CheckUnprocessed
  case class TransformImageRequest(imageId: Int, size: OptionalSize) extends ImageProcessorRequest with ApiRequest
  case class TransformImageResponse(alternative: Alternative)
  case class UploadImageRequest(filename: String, content: Array[Byte])
    extends ImageProcessorRequest with AuthorizedRequest


  def extractTags(meta: Option[ImageMetadata]): Seq[String] = {
    meta.map { m =>
      m.keywords ++ Seq(
        m.cameraModel,
        m.creationTime.map(d => DateTimeFormat.forPattern("yyyy-MM-dd").print(d))
      ).flatten
    }
  }.getOrElse(Seq.empty).map(_.toLowerCase)

}
