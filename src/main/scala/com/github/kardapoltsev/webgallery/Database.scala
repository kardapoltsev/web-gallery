package com.github.kardapoltsev.webgallery

import akka.actor.{Actor, ActorLogging}
import java.io.File
import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.exif.ExifSubIFDDirectory
import java.text.SimpleDateFormat
import spray.json.DefaultJsonProtocol
import com.github.kardapoltsev.webgallery.db.{Tag, Metadata, Image}
import com.github.kardapoltsev.webgallery.util.MetadataExtractor
import java.util.UUID
import org.mybatis.scala.config.Configuration



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
    case GetByAlbum(album) => sender() ! GetFilesResponse(files.filter(_.tags.contains(album)))
    case GetTags => sender() ! GetTagsResponse(files.flatMap(_.tags.map(_.name)).distinct)
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
        val dateTag = new SimpleDateFormat("yyyy-MM-dd").format(meta.creationTime)
        Image(name = f.getName, tags = Seq(Tag(dateTag)), mdata = meta)
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


  // mybatis stuff
  // Load datasource configuration from an external file
  val config = Configuration("myBatis.xml")

  // Add the data access function to the default namespace
  config ++= Tag.bind
  config ++= Image.bind

  // Build the session manager
  lazy val context = config.createPersistenceContext
}