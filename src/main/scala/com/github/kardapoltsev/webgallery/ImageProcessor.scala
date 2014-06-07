package com.github.kardapoltsev.webgallery

import akka.actor.{ActorLogging, Actor}
import java.io.File
import com.github.kardapoltsev.webgallery.db.{TransformImageParams, ImageAlternative, Tag, Image}
import com.github.kardapoltsev.webgallery.util.MetadataExtractor
import java.nio.file.{Files, Path}
import java.text.SimpleDateFormat
import java.util.UUID
import org.apache.commons.io.FilenameUtils
import com.github.kardapoltsev.webgallery.db.Image.ImageId
import akka.pattern.{ask, pipe}
import com.github.kardapoltsev.webgallery.Database.{FindAlternativeResponse, GetImageResponse}
import com.github.kardapoltsev.webgallery.processing.{OptionalSize, ScaleType, SpecificSize}
import akka.util.Timeout
import scala.concurrent.Future


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
      findUnprocessed flatMap process foreach { image =>
        databaseSelection ! Database.SaveImage(image)
      }
      scheduleCheck()

    case TransformImageRequest(imageId, transform) =>
      findOrCreateAlternative(imageId, transform) map { alt =>
        TransformImageResponse(alt)
      } pipeTo sender()
  }


  private def findOrCreateAlternative(imageId: ImageId, transform: TransformImageParams): Future[ImageAlternative] = {
    implicit val timeout = Timeout(10.seconds)

    databaseSelection ? Database.FindAlternative(imageId, transform) flatMap {
      case FindAlternativeResponse(Some(alt)) =>
        if(alt.transform == transform){
          Future.successful(alt)
        }
        else  {
          val newAlt = createAlternative(imageId, Configs.AlternativesDir + alt.filename, transform)
          databaseSelection ! Database.SaveAlternative(newAlt)
          Future.successful(newAlt)
        }
      case FindAlternativeResponse(None) =>
        databaseSelection ? Database.GetImage(imageId) map {
          case GetImageResponse(Some(image)) =>
            val alt = createAlternative(imageId, Configs.OriginalsDir + image.filename, transform)
            databaseSelection ! Database.SaveAlternative(alt)
            alt
        }
    }
  }


  private def createAlternative(imageId: ImageId, path: String, transform: TransformImageParams): ImageAlternative = {
    val scaleType =  if(transform.crop) ScaleType.FillDest else ScaleType.FitSource
    val alt = imageFrom(path).scaledTo(SpecificSize(transform.width, transform.height, scaleType))
    val altFilename = newFilename(path)
    alt.writeTo(Configs.AlternativesDir + altFilename)
    ImageAlternative(imageId, altFilename, transform)
  }


  private def findUnprocessed: Seq[File] = {
    new File(Configs.UnprocessedDir).listFiles().filter(_.isFile)
  }


  private def process(file: File): Option[Image] = {
    MetadataExtractor.process(file) match {
      case Some(meta) =>
        val dateTag = new SimpleDateFormat("yyyyMMdd").format(meta.creationTime)
        val filename = newFilename(file.getName)
        val image = Some(Image(
          name = file.getName,
          filename = filename,
          tags = Seq(Tag(dateTag)),
          mdata = meta))
        moveFile(file, new File(Configs.OriginalsDir + filename))
        image
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
  case class TransformImageRequest(imageId: ImageId, transform: TransformImageParams)
  case class TransformImageResponse(alternative: ImageAlternative)
}
