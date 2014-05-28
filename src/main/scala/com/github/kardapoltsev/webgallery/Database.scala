package com.github.kardapoltsev.webgallery

import akka.actor.{Actor, ActorLogging}
import java.io.File
import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.exif.ExifSubIFDDirectory
import java.text.SimpleDateFormat
import spray.json.DefaultJsonProtocol
import com.github.kardapoltsev.webgallery.db.{Metadata, Image}
import com.github.kardapoltsev.webgallery.util.MetadataExtractor

/**
 * Created by alexey on 5/26/14.
 */
class Database extends Actor with ActorLogging {
  import Database._
  import collection.mutable.Buffer

  val files: Buffer[Image] = readFiles()

  override def preStart(): Unit = {
//    log.debug(files.toString())
  }


  def receive: Receive = {
    case GetByAlbum(album) => sender() ! GetFilesResponse(files.filter(_.metadata.tags.contains(album)))
    case GetTags => sender() ! GetTagsResponse(files.flatMap(_.metadata.tags).distinct)
    case SaveImage(image) =>
      log.debug(s"saving image $image")
      files += image
  }


  //TODO: Remove this after db impl
  private def readFiles(): Buffer[Image] = {
    val dir = new File(Configs.OriginalsDir)
    dir.listFiles().filter(_.isFile).flatMap{ f =>
      log.debug(s"processing file ${f.getName}")
      MetadataExtractor.process(f) map { meta =>
        Image(f.getName, meta)
      }
    }.toBuffer
  }
}


object Database {
  case class GetByAlbum(album: String)
  case class GetFilesResponse(files: Seq[Image])
  case object GetTags
  case class GetTagsResponse(tags: Seq[String])
  case class SaveImage(image: Image)
}