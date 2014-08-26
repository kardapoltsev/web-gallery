package com.github.kardapoltsev.webgallery

import akka.actor.{Actor, ActorLogging}
import com.github.kardapoltsev.webgallery.db._
import spray.json.DefaultJsonProtocol
import scala.util.control.NonFatal
import com.github.kardapoltsev.webgallery.processing.{OptionalSize, SpecificSize}
import com.github.kardapoltsev.webgallery.db.gen
import com.github.kardapoltsev.webgallery.dto.ImageInfo
import com.github.kardapoltsev.webgallery.http._
import akka.event.LoggingReceive
import com.github.kardapoltsev.webgallery.routing.DatabaseRequest



/**
 * Created by alexey on 5/26/14.
 */
class Database extends Actor with ActorLogging with ImageHelper {
  import Database._
  import scalikejdbc._


  def receive: Receive = LoggingReceive {
    case r: AddTags =>
      withImage(r.imageId) { image =>
        addTags(r.imageId, r.tags)
        sender() ! SuccessResponse
      }
    case r: CreateImage => sender() ! CreateImageResponse(createImage(r))

    case GetImage(imageId) =>
      getImage(imageId) match {
        case Some(image) => sender() ! GetImageResponse(image)
        case None => sender() ! ErrorResponse.NotFound
      }

    case r: UpdateImage =>
      updateImage(r)
      sender() ! SuccessResponse

    case r: GetByTag =>  sender() ! GetImagesResponse(getImagesByTag(r.tagId, r.session.get.userId))

    case r: CreateAlternative =>
      withImage(r.imageId) { image =>
        sender() ! CreateAlternativeResponse(createAlternative(r))
      }

    case FindAlternative(imageId, transform) => sender() ! FindAlternativeResponse(findAlternative(imageId, transform))
  }


  private def findAlternative(imageId: Int, size: OptionalSize): Option[gen.Alternative] = {
    Alternative.find(imageId, size)
  }


  private def createAlternative(request: CreateAlternative): Alternative = {
    Alternative.create(request.imageId, request.filename, request.size)
  }


  @deprecated("send message to TagsManager", since = "2014-08-26")
  private def createTag(ownerId: UserId, name: String): Tag = {
    Tag.find(ownerId, name.toLowerCase) match {
      case Some(t) => t
      case None => Tag.create(ownerId, name.toLowerCase)
    }
  }


  private def addTags(imageId: Int, tags: Seq[TagId]): Unit = {
    tags.foreach { id =>
      ImageTag.create(imageId, id)
    }
  }


  private def createImage(request: CreateImage): Image = {
    val image = Image.create(request.name, request.filename, request.ownerId)
    request.meta.foreach{ m =>
      Metadata.create(image.id, m.cameraModel, m.creationTime)
    }
    val tagIds = request.tags.map(t => createTag(request.ownerId, t)).map(_.id)
    addTags(image.id, tagIds)
    image
  }


  private def updateImage(r: UpdateImage) = {
    r.params.tags.foreach{ tags =>
      val createdTags = tags.map(t => createTag(r.session.get.userId, t))
      createdTags.foreach { t =>
          try {
            ImageTag.create(r.imageId, t.id)
          } catch {
            case NonFatal(e) => //TODO: insert only new tags
          }
      }
    }
  }





  private def getImage(imageId: Int): Option[ImageInfo] = {
    Image.find(imageId) map { image =>
      val tags = Tag.findByImageId(image.id)
      ImageInfo(image, tags)
    }
  }


  private def getImagesByTag(tagId: TagId, userId: UserId): Seq[ImageInfo] = {
    Image.findByTag(tagId, userId) map { image =>
      val tags = Tag.findByImageId(image.id)
      ImageInfo(image, tags)
    }
  }

}


trait PrivilegedImageRequest extends PrivilegedRequest {
  def imageId: ImageId
  def subjectType = EntityType.Image
  def subjectId = imageId
}


object Database extends DefaultJsonProtocol {
  //Images
  case class AddTags(imageId: Int, tags: Seq[TagId]) extends PrivilegedImageRequest with DatabaseRequest
  case class GetImage(imageId: Int) extends AuthorizedRequest with DatabaseRequest
  case class GetByTag(tagId: TagId) extends AuthorizedRequest with DatabaseRequest
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
  
  case class UpdateImageParams(tags: Option[Seq[String]])
  object UpdateImageParams {
    implicit val _ = jsonFormat1(UpdateImageParams.apply)
  }
  case class UpdateImage(imageId: Int, params: UpdateImageParams) extends PrivilegedImageRequest with DatabaseRequest
  object UpdateImage {
    implicit val _ = jsonFormat2(UpdateImage.apply)
  }
  case class GetImagesResponse(images: Seq[ImageInfo])
  object GetImagesResponse {
    implicit val _ = jsonFormat1(GetImagesResponse.apply)
  }

  //Alternatives
  case class CreateAlternative(imageId: Int, filename: String, size: OptionalSize)
    extends ApiRequest with DatabaseRequest
  case class CreateAlternativeResponse(alternative: Alternative)
  
  case class FindAlternative(imageId: Int, size: OptionalSize) extends ApiRequest with DatabaseRequest
  case class FindAlternativeResponse(alternative: Option[Alternative])


  def cleanDatabase(): Unit = {
    import scalikejdbc._
    DB autoCommit { implicit s =>
      sql"delete from image; delete from tags; delete from users; delete from credentials;".execute().apply()
    }
  }


  private def init(): Unit = {
    import scalikejdbc.config._
    DBs.setupAll()
  }
  init()
}
