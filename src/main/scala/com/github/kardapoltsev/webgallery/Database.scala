package com.github.kardapoltsev.webgallery

import akka.actor.{Actor, ActorLogging}
import com.github.kardapoltsev.webgallery.db._
import com.github.kardapoltsev.webgallery.util.MetadataExtractor
import spray.json.DefaultJsonProtocol
import scala.util.control.NonFatal
import com.github.kardapoltsev.webgallery.processing.SpecificSize
import com.github.kardapoltsev.webgallery.db.gen
import scalikejdbc.AutoSession
import com.github.kardapoltsev.webgallery.dto.ImageInfo



/**
 * Created by alexey on 5/26/14.
 */
class Database extends Actor with ActorLogging {
  import Database._
  import collection.mutable.Buffer
  import scalikejdbc._


  def receive: Receive = {
    case r: CreateImage => sender() ! CreateImageResponse(createImage(r))

    case GetImage(imageId) => sender() ! GetImageResponse(getImage(imageId))

    case r: UpdateImage =>
      respond {
        updateImage(r)
        sender() ! SuccessResponse
      }


    case GetByTag(tag) =>  sender() ! GetImagesResponse(getImagesByTag(tag))

    case CreateTag(name) => sender() ! CreateTagResponse(createTag(name))

    case GetTags => sender() ! GetTagsResponse(getTags)

    case GetTags(imageId) => sender() ! GetTagsResponse(getTags(imageId))

    case SearchTags(query) => sender() ! GetTagsResponse(searchTags(query))

    case AddTags(imageId, tags) =>
      respond {
        addTags(imageId, tags)
        sender() ! SuccessResponse
      }

    case r: CreateAlternative =>
      respond {
        sender() ! CreateAlternativeResponse(createAlternative(r))
      }

    case FindAlternative(imageId, transform) => sender() ! FindAlternativeResponse(findAlternative(imageId, transform))
  }


  /**
   * Try execute action, send [[ErrorResponse]] to sender if action failed
   */
  private def respond(action: => Unit): Unit = {
    try {
      action
    } catch {
      case NonFatal(e) => sender() ! ErrorResponse
    }
  }


  private def findAlternative(imageId: Int, size: SpecificSize): Option[gen.Alternative] = {
    Alternative.find(imageId, size)
  }


  private def createAlternative(request: CreateAlternative): Alternative = {
    Alternative.create(request.imageId, request.filename, request.size)
  }


  private def addTags(imageId: Int, tags: Seq[String]): Unit = {
    val saved = tags.map { name => createTag(name)}
    saved.foreach { tag =>
      ImageTag.create(imageId, tag.id)
    }
  }


  private def createImage(request: CreateImage): Image = {
    val image = Image.create(request.name, request.filename)
    request.meta.foreach{ m =>
      Metadata.create(image.id, m.cameraModel, m.creationTime)
    }
    addTags(image.id, request.tags)
    image
  }


  private def updateImage(r: UpdateImage) = {
    r.params.tags.foreach{ tags =>
      val createdTags = tags.map(t => createTag(t))
      createdTags.foreach { t =>
          try {
            ImageTag.create(r.imageId, t.id)
          } catch {
            case NonFatal(e) => //TODO: insert only new tags
          }
      }
    }
  }


//  private def create(meta: Metadata): Metadata = {
//    Metadata.insert(meta)
//    meta
//  }

  
  private def createTag(name: String): Tag = {
    Tag.find(name) match {
      case Some(t) => t
      case None => Tag.create(name)
    }
  }


  private def getTags(imageId: Int): Seq[Tag] = {
    Tag.findByImageId(imageId)
  }


  private def getTags: Seq[Tag] = {
    Tag.findAll()
  }


  private def searchTags(query: String): Seq[Tag] = {
    Tag.search(query)
  }


  private def getImage(imageId: Int): Option[ImageInfo] = {
    Image.find(imageId) map { image =>
      val tags = Tag.findByImageId(image.id)
      ImageInfo(image, tags)
    }
  }


  private def getImagesByTag(tag: String): Seq[ImageInfo] = {
    Image.findByTag(tag) map { image =>
      val tags = Tag.findByImageId(image.id)
      ImageInfo(image, tags)
    }
  }

}


object Database extends DefaultJsonProtocol {
  //Tags
  case class AddTags(imageId: Int, tags: Seq[String])
  case object GetTags
  case class GetTags(imageId: Int)
  case class GetTagsResponse(tags: Seq[Tag])
  case class CreateTag(name: String)
  case class CreateTagResponse(tag: Tag)
  case class SearchTags(query: String)
  
  //Images
  case class GetImage(imageId: Int)
  case class GetByTag(tag: String)
  case class GetImageResponse(image: Option[ImageInfo])
  
  case class CreateImage(name: String, filename: String, meta: Option[ExifMetadata], tags: Seq[String])
  case class CreateImageResponse(image: Image)
  
  case class UpdateImage(imageId: Int, params: UpdateImageParams)
  case class UpdateImageParams(tags: Option[Seq[String]])
  case class GetImagesResponse(images: Seq[ImageInfo])

  //Alternatives
  case class CreateAlternative(imageId: Int, filename: String, size: SpecificSize)
  case class CreateAlternativeResponse(alternative: Alternative)
  
  case class FindAlternative(imageId: Int, size: SpecificSize)
  case class FindAlternativeResponse(alternative: Option[Alternative])


  trait InternalResponse
  case object SuccessResponse extends InternalResponse
  case object ErrorResponse extends InternalResponse


  def cleanDatabase(): Unit = {
    import scalikejdbc._
    implicit val s = AutoSession
    sql"delete from image; delete from tag;".execute().apply()
  }


  private def init(): Unit = {
    import scalikejdbc.config._
    DBs.setupAll()
  }
  init()
}
