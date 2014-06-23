package com.github.kardapoltsev.webgallery

import akka.actor.{Actor, ActorLogging}
import com.github.kardapoltsev.webgallery.db._
import com.github.kardapoltsev.webgallery.util.MetadataExtractor
import spray.json.DefaultJsonProtocol
import scala.util.control.NonFatal
import com.github.kardapoltsev.webgallery.processing.{OptionalSize, SpecificSize}
import com.github.kardapoltsev.webgallery.db.gen
import scalikejdbc.AutoSession
import com.github.kardapoltsev.webgallery.dto.ImageInfo
import com.github.kardapoltsev.webgallery.http.{AuthorizedRequest, ApiRequest, ErrorResponse, SuccessResponse}
import akka.event.LoggingReceive
import com.github.kardapoltsev.webgallery.routing.DatabaseRequest



/**
 * Created by alexey on 5/26/14.
 */
class Database extends Actor with ActorLogging {
  import Database._
  import collection.mutable.Buffer
  import scalikejdbc._


  def dummy(id: UserId) = {}

  def receive: Receive = LoggingReceive {
    case r: CreateImage => sender() ! CreateImageResponse(createImage(r))

    case GetImage(imageId) =>
      getImage(imageId) match {
        case Some(image) => sender() ! GetImageResponse(image)
        case None => sender() ! ErrorResponse.NotFound
      }

    case r: UpdateImage =>
      respond {
        updateImage(r)
        sender() ! SuccessResponse
      }


    case r: GetByTag =>  sender() ! GetImagesResponse(getImagesByTag(r.tag, r.session.get.userId))

    case CreateTag(name) => sender() ! CreateTagResponse(createTag(name))

    case GetTags => sender() ! GetTagsResponse(getTags)

    case GetImageTags(imageId) => sender() ! GetTagsResponse(getTags(imageId))

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
   * Try execute action, send [[com.github.kardapoltsev.webgallery.http.ErrorResponse]] to sender if action failed
   */
  private def respond(action: => Unit): Unit = {
    try {
      action
    } catch {
      case NonFatal(e) => sender() ! ErrorResponse
    }
  }


  private def findAlternative(imageId: Int, size: OptionalSize): Option[gen.Alternative] = {
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
    val image = Image.create(request.name, request.filename, request.ownerId)
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


  private def getImagesByTag(tag: String, userId: UserId): Seq[ImageInfo] = {
    Image.findByTag(tag, userId) map { image =>
      val tags = Tag.findByImageId(image.id)
      ImageInfo(image, tags)
    }
  }

}


object Database extends DefaultJsonProtocol {
  //Tags
  case class AddTags(imageId: Int, tags: Seq[String]) extends ApiRequest with DatabaseRequest
  case object GetTags extends ApiRequest with DatabaseRequest
  case class GetImageTags(imageId: Int) extends ApiRequest with DatabaseRequest
  case class GetTagsResponse(tags: Seq[Tag])
  object GetTagsResponse {
    implicit val _ = jsonFormat1(GetTagsResponse.apply)
  }

  case class CreateTag(name: String) extends ApiRequest with DatabaseRequest
  case class CreateTagResponse(tag: Tag)
  case class SearchTags(query: String) extends ApiRequest with DatabaseRequest
  
  //Images
  case class GetImage(imageId: Int) extends ApiRequest with DatabaseRequest
  case class GetByTag(tag: String) extends AuthorizedRequest with DatabaseRequest
  case class GetImageResponse(image: ImageInfo)
  object GetImageResponse {
    implicit val _ = jsonFormat1(GetImageResponse.apply)
  }
  
  case class CreateImage(
    ownerId: UserId,
    name: String,
    filename: String,
    meta: Option[ImageMetadata],
    tags: Seq[String]) extends DatabaseRequest
  case class CreateImageResponse(image: Image)
  
  case class UpdateImage(imageId: Int, params: UpdateImageParams) extends ApiRequest with DatabaseRequest
  case class UpdateImageParams(tags: Option[Seq[String]])
  case class GetImagesResponse(images: Seq[ImageInfo])

  //Alternatives
  case class CreateAlternative(imageId: Int, filename: String, size: SpecificSize) extends ApiRequest with DatabaseRequest
  case class CreateAlternativeResponse(alternative: Alternative)
  
  case class FindAlternative(imageId: Int, size: OptionalSize) extends ApiRequest with DatabaseRequest
  case class FindAlternativeResponse(alternative: Option[Alternative])


  def cleanDatabase(): Unit = {
    import scalikejdbc._
    DB autoCommit { implicit s =>
      sql"delete from image; delete from tag; delete from users; delete from credentials;".execute().apply()
    }
  }


  private def init(): Unit = {
    import scalikejdbc.config._
    DBs.setupAll()
  }
  init()
}
