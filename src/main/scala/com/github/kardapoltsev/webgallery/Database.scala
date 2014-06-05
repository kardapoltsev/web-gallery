package com.github.kardapoltsev.webgallery

import akka.actor.{Actor, ActorLogging}
import java.io.File
import java.text.SimpleDateFormat
import com.github.kardapoltsev.webgallery.db.{ImagesTags, Tag, Metadata, Image}
import com.github.kardapoltsev.webgallery.util.MetadataExtractor
import org.mybatis.scala.config.Configuration
import org.mybatis.scala.session.Session
import spray.json.DefaultJsonProtocol
import com.github.kardapoltsev.webgallery.db.Image.ImageId



/**
 * Created by alexey on 5/26/14.
 */
class Database extends Actor with ActorLogging {
  import Database._
  import collection.mutable.Buffer


  def receive: Receive = {
    case GetByTag(tag) => sender() ! GetImagesResponse(getImagesByTag(tag))

    case CreateTag(tag) =>
      val t = db.transaction { implicit s =>
        saveTag(Tag(tag))
      }
      sender() ! CreateTagResponse(t)

    case GetImage(imageId) => sender() ! GetImageResponse(getImage(imageId))

    case UpdateImage(imageId, params) =>
      db.transaction { implicit s =>
        params.tags.foreach(_.foreach { t =>
          val tag = saveTag(t)
          Image.addTag(ImagesTags(imageId, tag.id))
        })
      }
      sender() ! SuccessResponse

    case GetTags => sender() ! GetTagsResponse(getTags)

    case SearchTags(query) => sender() ! GetTagsResponse(searchTags(query))

    case AddTags(imageId, tags) => addTags(imageId, tags)

    case SaveImage(image) =>
      log.debug(s"saving image $image")
      saveImage(image)
  }


  private def addTags(imageId: Int, tags: Seq[String]): Unit = {
    db.transaction{ implicit s =>
      val saved = tags.map{ name => saveTag(Tag(name))}
      saved.foreach{ tag =>
        Image.addTag(ImagesTags(imageId, tag.id))
      }
    }
  }


  private def saveImage(image: Image): Image = {
    db.transaction { implicit session =>
      val meta = image.metadata map saveMetadata
      val tags = image.tags map saveTag
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

  
  private def saveTag(tag: Tag)(implicit s: Session): Tag = {
      Tag.getByName(tag.name) match {
        case Some(t) => t
        case None =>
          Tag.insert(tag)
          tag
      }
  }


  private def getTags: Seq[Tag] = {
    db.transaction { implicit s =>
      Tag.getTags()
    }
  }


  private def searchTags(query: String): Seq[Tag] = {
    db.transaction { implicit s =>
      Tag.searchByName(query)
    }
  }


  private def getImage(imageId: Int): Option[Image] = {
    db.transaction{ implicit s =>
      Image.getById(imageId)
    }
  }


  private def getImagesByTag(tag: String): Seq[Image] = {
    db.transaction { implicit s =>
      Image.getByTag(tag)
    }
  }
}


object Database extends DefaultJsonProtocol {
  //Tags
  case class GetByTag(album: String)
  case class AddTags(imageId: Int, tags: Seq[String])
  case object GetTags
  case class GetTagsResponse(tags: Seq[Tag])
  case class CreateTag(name: String)
  case class CreateTagResponse(tag: Tag)
  case class SearchTags(query: String)
  
  //Images
  case class GetImage(imageId: Int)
  case class GetImageResponse(image: Option[Image])
  case class SaveImage(image: Image)
  case class UpdateImage(imageId: ImageId, params: UpdateImageParams)
  case class UpdateImageParams(tags: Option[Seq[Tag]])

  case class GetImagesResponse(images: Seq[Image])

  trait InternalResponse
  case object SuccessResponse extends InternalResponse


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
