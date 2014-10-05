package com.github.kardapoltsev.webgallery.tags


import akka.actor.{Props, Actor, ActorLogging}
import com.github.kardapoltsev.webgallery.PrivilegedImageRequest
import com.github.kardapoltsev.webgallery.acl.{Permissions, PrivilegedTagRequest}
import com.github.kardapoltsev.webgallery.db._
import com.github.kardapoltsev.webgallery.http._
import com.github.kardapoltsev.webgallery.routing.TagsManagerRequest
import com.github.kardapoltsev.webgallery.util.Hardcoded.ActorNames
import scalikejdbc.DB
import spray.json.DefaultJsonProtocol



/**
 * Created by alexey on 8/26/14.
 */
class TagsManager extends Actor with ActorLogging {
  import com.github.kardapoltsev.webgallery.tags.TagsManager._

  private val eventListener = context.actorOf(Props[EventListener], ActorNames.EventListener)

  def receive: Receive = processGetRecentTags orElse processGetTag orElse processUpdateTag orElse {

    case r: CreateTag => sender() ! CreateTagResponse(createTag(r.session.get.userId, r.name))

    case GetTags(userId) => sender() ! GetTagsResponse(getTagsByUserId(userId))

    case GetImageTags(imageId) => sender() ! GetTagsResponse(getTags(imageId))

    case SearchTags(query) => sender() ! GetTagsResponse(searchTags(query))

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


  private def createTag(ownerId: UserId, name: String): Tag = {
    Tag.find(ownerId, name.toLowerCase) match {
      case Some(t) => t
      case None =>
        DB.localTx { implicit s =>
          val t = Tag.create(ownerId, name.toLowerCase)
          Acl.create(t.id, ownerId)
          t
        }
    }
  }


  private def getTags(imageId: Int): Seq[Tag] = {
    Tag.findByImageId(imageId)
  }


  private def getTagsByUserId(userId: UserId): Seq[Tag] = {
    Tag.findByUserId(userId)
  }


  private def searchTags(query: String): Seq[Tag] = {
    Tag.search(query)
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
