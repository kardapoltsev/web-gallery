package com.github.kardapoltsev.webgallery

import akka.actor.{Actor, ActorLogging}
import java.io.File
import java.text.SimpleDateFormat
import com.github.kardapoltsev.webgallery.db.{ImagesTags, Tag, Metadata, Image}
import com.github.kardapoltsev.webgallery.util.MetadataExtractor
import org.mybatis.scala.config.Configuration
import org.mybatis.scala.session.Session



/**
 * Created by alexey on 5/26/14.
 */
class Database extends Actor with ActorLogging {
  import Database._
  import collection.mutable.Buffer


  def receive: Receive = {
    case GetByTag(tag) => sender() ! GetFilesResponse(getImagesByTag(tag))
    case GetTags => sender() ! GetTagsResponse(getTags)
    case SaveImage(image) =>
      log.debug(s"saving image $image")
      saveImage(image)
  }

  
  private def saveImage(image: Image): Image = {
    db.transaction { implicit session =>
      val meta = image.metadata map saveMetadata
      val tags = saveTags(image.tags)
      val img = image.copy(tags = tags, mdata = meta.getOrElse(null))
      Image.insert(img)
      tags foreach {t =>
        Image.addTag(ImagesTags(img.id, t.id))
      }
      img
    }
  }


  private def saveMetadata(meta: Metadata)(implicit s: Session): Metadata = {
    Metadata.insert(meta)
    meta
  }

  
  private def saveTags(tags: Seq[Tag])(implicit s: Session): Seq[Tag] = {
    tags.map { t =>
      Tag.selectByName(t.name) match {
        case Some(tag) =>
          tag
        case None =>
          Tag.insert(t)
          t
      }
    }
  }


  private def getTags: Seq[Tag] = {
    db.transaction { implicit s =>
      Tag.getTags()
    }
  }


  private def getImagesByTag(tag: String): Seq[Image] = {
    db.transaction { implicit s =>
      Image.getByTag(tag)
    }
  }
}


object Database {
  case class GetByTag(album: String)
  case class GetFilesResponse(files: Seq[Image])
  case object GetTags
  case class GetTagsResponse(tags: Seq[Tag])
  case class SaveImage(image: Image)


  // mybatis stuff
  // Load datasource configuration from an external file
  val config = Configuration("myBatis.xml")

  // Add the data access function to the default namespace
  config ++= Tag.bind
  config ++= Image.bind
  config ++= Metadata.bind

  // Build the session manager
  lazy val db = config.createPersistenceContext
}