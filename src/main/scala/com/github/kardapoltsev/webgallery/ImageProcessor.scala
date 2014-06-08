package com.github.kardapoltsev.webgallery

import akka.actor.{ActorLogging, Actor}
import java.io.File
import com.github.kardapoltsev.webgallery.db._
import com.github.kardapoltsev.webgallery.util.MetadataExtractor
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


/**
 * Created by alexey on 5/27/14.
 */
class ImageProcessor extends Actor with ActorLogging with WebGalleryActorSelection {
  import concurrent.duration._
  import ImageProcessor._
  import com.github.kardapoltsev.webgallery.processing.Java2DImageImplicits._
  import context.dispatcher

  override def preStart(): Unit = {
    scheduleCheck()
  }

  def receive: Receive = {
    case CheckUnprocessed =>
      findUnprocessed flatMap process
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
    val altFilename = newFilename(path)
    alt.writeTo(Configs.AlternativesDir + altFilename)
    CreateAlternative(imageId, altFilename, size)
  }


  private def findUnprocessed: Seq[File] = {
    new File(Configs.UnprocessedDir).listFiles().filter(_.isFile)
  }


  private def process(file: File): Option[CreateImage] = {
    MetadataExtractor.process(file) match {
      case Some(meta) =>
        val dateTag = new SimpleDateFormat("yyyyMMdd").format(meta.creationTime)
        val filename = newFilename(file.getName)
        val request = Some(CreateImage(file.getName, filename))
        moveFile(file, new File(Configs.OriginalsDir + filename))
        request
      case None =>
        file.delete()
        None
    }
  }


  private def newFilename(old: String): String = {
    val ext = FilenameUtils.getExtension(old)
    UUID.randomUUID().toString + "." + ext
  }


  private def moveFile(source: File, destination: File): Path = {
    import java.nio.file.{StandardCopyOption, Files}
    Files.move(source.toPath, destination.toPath, StandardCopyOption.REPLACE_EXISTING)
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
}
