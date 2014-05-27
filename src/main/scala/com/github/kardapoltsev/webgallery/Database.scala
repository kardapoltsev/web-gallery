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

  val files = readFiles()

  override def preStart(): Unit = {
//    log.debug(files.toString())
  }

  private def readFiles(): Seq[Image] = {
    val dir = new File(Configs.OriginalsDir)
    dir.listFiles().filter(_.isFile).map{ f =>
      val meta = MetadataExtractor.process(f)
      Image(f.getName, meta)
    }.toSeq
  }


  def receive: Receive = {
    case GetByAlbum(album) => sender() ! GetFilesResponse(files.filter(_.metadata.tags.contains(album)))
    case GetTags => sender() ! GetTagsResponse(files.flatMap(_.metadata.tags).distinct)
  }
}




object Database {
  case class GetByAlbum(album: String)
  case class GetFilesResponse(files: Seq[Image])
  case object GetTags
  case class GetTagsResponse(tags: Seq[String])
}