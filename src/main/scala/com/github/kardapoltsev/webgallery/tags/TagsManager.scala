package com.github.kardapoltsev.webgallery.tags


import akka.actor.{Props, Actor, ActorLogging}
import com.github.kardapoltsev.webgallery.PrivilegedImageRequest
import com.github.kardapoltsev.webgallery.acl.{Permissions, PrivilegedTagRequest}
import com.github.kardapoltsev.webgallery.db._
import com.github.kardapoltsev.webgallery.es.UserCreated
import com.github.kardapoltsev.webgallery.http._
import com.github.kardapoltsev.webgallery.routing.TagsManagerRequest
import com.github.kardapoltsev.webgallery.util.Hardcoded.ActorNames
import scalikejdbc.{DBSession, DB}
import spray.json.DefaultJsonProtocol



/**
 * Created by alexey on 8/26/14.
 */
class TagsManager extends Actor with ActorLogging with EventListener {
  import com.github.kardapoltsev.webgallery.tags.TagsManager._

  def receive: Receive = Seq(processGetRecentTags, processGetTag, processUpdateTag, handleEvents, processCreateTag,
    processGetTags, processGetImageTags, processSearchTags
  ) reduce (_ orElse _)


  private def processGetTags: Receive = {
    case GetTags(userId) =>
      val tags = Tag.findByUserId(userId)
      sender() ! GetTagsResponse(tags)
  }


  private def processGetImageTags: Receive = {
    case GetImageTags(imageId) =>
      val tags = Tag.findByImageId(imageId)
      sender() ! GetTagsResponse(tags)
  }


  private def processSearchTags: Receive = {
    case SearchTags(query) =>
      val tags = Tag.search(query)
      sender() ! GetTagsResponse(tags)
  }


  private def processUpdateTag: Receive = {
    case UpdateTag(tagId, name, coverId) =>
      Tag.find(tagId) match {
        case Some(tag) =>
          name.foreach(n => Tag.setName(tagId, n))
          coverId.foreach(cId => Tag.setCoverId(tagId, cId, manual = true))
          sender() ! SuccessResponse
        case None => sender() ! ErrorResponse.NotFound
      }
  }


  private def processGetTag: Receive = {
    case GetTag(tagId) =>
      val response = Tag.find(tagId) match {
        case Some(t) => GetTagResponse(t)
        case None => ErrorResponse.NotFound
      }
      sender() ! response
  }


  private def processGetRecentTags: Receive = {
    case r @ GetRecentTags(userId) =>
      val tags = Tag.getRecentTags(userId, r.offset, r.limit)
      sender() ! GetTagsResponse(tags)
  }


  private def processCreateTag: Receive = {
    case r @CreateTag(name) =>
      val ownerId = r.session.get.userId
      val tag = DB localTx { implicit s =>
        createTag(name, ownerId)
      }
      sender() ! CreateTagResponse(tag)
  }


  protected def createTag(name: String, ownerId: UserId)(implicit s: DBSession): Tag = {
    Tag.find(ownerId, name.toLowerCase) match {
      case Some(t) =>
        if(t.auto) {
          val updated = t.copy(auto = false)
          Tag.save(updated)
          updated
        } else {
          t
        }
      case None =>
        DB.localTx { implicit s =>
          val t = Tag.create(ownerId, name.toLowerCase, system = false, auto = false)
          Acl.create(t.id, ownerId)
          t
        }
    }
  }

}


object TagsManager extends DefaultJsonProtocol {

  case class GetTag(tagId: TagId) extends PrivilegedTagRequest with TagsManagerRequest {
    def permissions = Permissions.Read
  }
  case class GetTagResponse(tag: Tag)
  object GetTagResponse {
    implicit val _ = jsonFormat1(GetTagResponse.apply)
  }
  case class GetTags(userId: UserId) extends AuthorizedRequest with TagsManagerRequest


  case class GetRecentTags(userId: UserId) extends AuthorizedRequest with TagsManagerRequest with Pagination


  case class GetImageTags(imageId: Int) extends PrivilegedImageRequest with TagsManagerRequest {
    def permissions = Permissions.Read
  }
  case class GetTagsResponse(tags: Seq[Tag])
  object GetTagsResponse {
    implicit val _ = jsonFormat1(GetTagsResponse.apply)
  }


  case class CreateTag(name: String) extends AuthorizedRequest with TagsManagerRequest
  object CreateTag {
    implicit val _ = jsonFormat1(CreateTag.apply)
  }
  case class CreateTagResponse(tag: Tag)
  object CreateTagResponse {
    implicit val _ = jsonFormat1(CreateTagResponse.apply)
  }


  case class SearchTags(query: String) extends AuthorizedRequest with TagsManagerRequest


  case class UpdateTag(tagId: TagId, name: Option[String], coverId: Option[ImageId])
      extends PrivilegedTagRequest with TagsManagerRequest {
    def permissions = Permissions.Write
  }

}
