package com.github.kardapoltsev.webgallery

import akka.actor.{ActorLogging, Actor}
import java.io.File
import com.github.kardapoltsev.webgallery.db.Image
import com.github.kardapoltsev.webgallery.util.MetadataExtractor
import java.nio.file.Path

/**
 * Created by alexey on 5/27/14.
 */
class ImageProcessor extends Actor with ActorLogging with WebGalleryActorSelection {
  import ImageProcessor._

  override def preStart(): Unit = {
    scheduleCheck()
  }

  def receive: Receive = {
    case CheckUnprocessed =>
      findUnprocessed flatMap process foreach { image =>
        databaseSelection ! Database.SaveImage(image)
      }
      scheduleCheck()
  }


  private def findUnprocessed: Seq[File] = {
    new File(Configs.UnprocessedDir).listFiles().filter(_.isFile)
  }


  private def process(file: File): Option[Image] = {
    MetadataExtractor.process(file) match {
      case Some(meta) =>
        val original = moveFile(file, new File(Configs.OriginalsDir + file.getName))
        createAlternatives(original)
        Some(Image(file.getName, meta))
      case None =>
        file.delete()
        None
    }
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
}
