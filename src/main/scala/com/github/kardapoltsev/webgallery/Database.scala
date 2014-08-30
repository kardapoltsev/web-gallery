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

    case GetImage(imageId) =>
      getImage(imageId) match {
        case Some(image) => sender() ! GetImageResponse(image)
        case None => sender() ! ErrorResponse.NotFound
      }

    case r: UpdateImage =>
      updateImage(r)
      sender() ! SuccessResponse

    case r: GetByTag =>  sender() ! GetImagesResponse(getImagesByTag(r.tagId, r.session.get.userId))

  }


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
