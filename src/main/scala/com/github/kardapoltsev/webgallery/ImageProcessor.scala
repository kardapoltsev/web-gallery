package com.github.kardapoltsev.webgallery

import akka.actor.{ActorLogging, Actor}
import java.io.File
import com.github.kardapoltsev.webgallery.db._
import com.github.kardapoltsev.webgallery.util.{FilesUtil, MetadataExtractor}
import java.nio.file.{Files, Path}
import java.text.SimpleDateFormat
import java.util.UUID
import org.apache.commons.io.FilenameUtils
import akka.pattern.{ask, pipe}
import com.github.kardapoltsev.webgallery.Database._
import akka.util.Timeout
import scala.concurrent.Future
import com.github.kardapoltsev.webgallery.Database.CreateAlternative
import com.github.kardapoltsev.webgallery.Database.FindAlternativeResponse
import com.github.kardapoltsev.webgallery.Database.CreateImage
import com.github.kardapoltsev.webgallery.processing.SpecificSize
import scala.Some
import com.github.kardapoltsev.webgallery.Database.GetImageResponse
import org.joda.time.format.DateTimeFormat
import scala.util.{Success, Failure}
import scala.util.control.NonFatal



/**
 * Created by alexey on 5/27/14.
 */
class ImageProcessor extends Actor with ActorLogging with WebGalleryActorSelection {
  import concurrent.duration._
  import ImageProcessor._
  import com.github.kardapoltsev.webgallery.processing.Java2DImageImplicits._
  import context.dispatcher
  implicit val timeout = Timeout(1.second)

  override def preStart(): Unit = {
    scheduleCheck()
  }

  def receive: Receive = {
    case CheckUnprocessed =>
      findUnprocessed foreach process
      scheduleCheck()
    case TransformImageRequest(imageId, size) =>
      findOrCreateAlternative(imageId, size) map { alt =>
        TransformImageResponse(alt)
      } pipeTo sender()
  }


  private def findOrCreateAlternative(imageId: Int, size: SpecificSize): Future[Alternative] = {
    implicit val timeout = Timeout(10.seconds)

    databaseSelection ? Database.FindAlternative(imageId, size) flatMap {
      case FindAlternativeResponse(Some(alt)) =>
        if(alt.size == size){
          Future.successful(alt)
        }
        else  {
          val request = createAlternative(imageId, Configs.AlternativesDir + alt.filename, size)
          databaseSelection ? request map {
            case CreateAlternativeResponse(alternative) => alternative
          }
        }
      case FindAlternativeResponse(None) =>
        databaseSelection ? Database.GetImage(imageId) flatMap {
          case GetImageResponse(Some(image)) =>
            val request = createAlternative(imageId, Configs.OriginalsDir + image.filename, size)
            databaseSelection ? request map {
              case CreateAlternativeResponse(alternative) => alternative
            }
        }
    }
  }


  private def createAlternative(imageId: Int, path: String, size: SpecificSize): CreateAlternative = {
    val alt = imageFrom(path) scaledTo size
    val altFilename = FilesUtil.newFilename(path)
    alt.writeTo(Configs.AlternativesDir + altFilename)
    CreateAlternative(imageId, altFilename, size)
  }


  private def findUnprocessed: Seq[File] = {
    new File(Configs.UnprocessedDir).listFiles().filter(_.isFile)
  }


  private def process(file: File): Future[Option[Image]] = {
    val meta = MetadataExtractor.process(file)
    val tags = extractTags(meta)
    val filename = FilesUtil.newFilename(file.getName)
    databaseSelection ? CreateImage(file.getName, filename, meta, tags) map {
      case CreateImageResponse(image) =>
        FilesUtil.moveFile(file, Configs.OriginalsDir + filename)
        Some(image)
    } recover {
      case NonFatal(e) =>
        log.error(s"couldn't process file $file", e)
        file.delete()
        None
    }
  }


  private def scheduleCheck(): Unit = {
    import concurrent.duration._
    import context.dispatcher
    context.system.scheduler.scheduleOnce(Configs.CheckUnprocessedInterval.seconds, self, CheckUnprocessed)
  }
}


object ImageProcessor {
  case object CheckUnprocessed
  case class TransformImageRequest(imageId: Int, size: SpecificSize)
  case class TransformImageResponse(alternative: Alternative)


  def extractTags(meta: Option[ImageMetadata]): Seq[String] = {
    meta.map{ m =>
      m.keywords ++ Seq(
        m.cameraModel.map(_.toLowerCase),
        m.creationTime.map(d => DateTimeFormat.forPattern("yyyy-MM-dd").print(d))
      ).flatten
    }
  }.getOrElse(Seq.empty)

}
