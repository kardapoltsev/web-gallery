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
import com.github.kardapoltsev.webgallery.Database.GetImageResponse
import com.github.kardapoltsev.webgallery.processing.{OptionalSize, ScaleType, SpecificSize}
import akka.util.Timeout


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
      implicit val timeout = Timeout(5.seconds)
      databaseSelection ? Database.GetImage(imageId) map {
        case GetImageResponse(Some(image)) =>
          val scaleType =  if(transform.crop) ScaleType.FillDest else ScaleType.FitSource
          val alt = imageFrom(Configs.OriginalsDir + image.filename)
            .scaledTo(SpecificSize(transform.width, transform.height, scaleType))
          val filename = newFilename(image.filename)
          alt.writeTo(Configs.AlternativesDir + filename)
          TransformImageResponse(ImageAlternative(image.id, filename, transform))
      } pipeTo sender()
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
        val original = moveFile(file, new File(Configs.OriginalsDir + filename))
        createAlternatives(original)
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


  private def createAlternatives(path: Path) = {
    val cmd = Seq("convert", "-resize", "64x64!", path.toString, Configs.ThumbnailsDir + path.getFileName.toString)
    log.debug(s"executing cmd: $cmd")
    import sys.process._
    val exitCode = cmd.!
    if(exitCode != 0){
      log.error(s"Failed to execute cmd: `${cmd.mkString(" ")}'")
    }
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
