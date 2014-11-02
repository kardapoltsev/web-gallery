package com.github.kardapoltsev.webgallery.tags


import akka.actor.{Props, Actor, ActorLogging}
import akka.event.LoggingReceive
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
object TagsManager extends DefaultJsonProtocol {

  case class GetTag(tagId: TagId) extends PrivilegedTagRequest with TagsManagerRequest {
    def permissions = Permissions.Read
  }
  case class GetTagResponse(tag: Tag) extends ApiResponse
  object GetTagResponse {
    implicit val _ = jsonFormat1(GetTagResponse.apply)
  }
  case class GetTags(userId: UserId) extends AuthorizedRequest with TagsManagerRequest


  case class GetRecentTags(userId: UserId) extends AuthorizedRequest with TagsManagerRequest with Pagination


  case class GetTagsResponse(tags: Seq[Tag]) extends ApiResponse
  object GetTagsResponse {
    implicit val _ = jsonFormat1(GetTagsResponse.apply)
  }


  case class CreateTag(name: String) extends AuthorizedRequest with TagsManagerRequest
  object CreateTag {
    implicit val _ = jsonFormat1(CreateTag.apply)
  }
  case class CreateTagResponse(tag: Tag) extends ApiResponse
  object CreateTagResponse {
    implicit val _ = jsonFormat1(CreateTagResponse.apply)
  }


  case class SearchTags(query: String) extends AuthorizedRequest with TagsManagerRequest with Pagination


  case class UpdateTag(tagId: TagId, name: Option[String], coverId: Option[ImageId])
      extends PrivilegedTagRequest with TagsManagerRequest {
    def permissions = Permissions.Write
  }

}


class TagsManager extends Actor with ActorLogging with EventListener {
  import com.github.kardapoltsev.webgallery.tags.TagsManager._
  import marshalling._
  import context.dispatcher

  def receive: Receive = LoggingReceive(
    Seq(processGetRecentTags, processGetTag, processUpdateTag, handleEvents, processCreateTag, processGetTags,
      processSearchTags) reduceLeft (_ orElse _)
  )


  private def processGetTags: Receive = {
    case r @ GetTags(userId) =>
      val tags = Tag.findByUserId(userId)
      r.complete(GetTagsResponse(tags))
  }


  private def processSearchTags: Receive = {
    case r @ SearchTags(query) =>
      val tags = Tag.search(query, r.offset, r.limit)
      r.complete(GetTagsResponse(tags))
  }


  private def processUpdateTag: Receive = {
    case r @ UpdateTag(tagId, name, coverId) =>
      Tag.find(tagId) match {
        case Some(tag) =>
          name.foreach(n => Tag.setName(tagId, n))
          coverId.foreach(cId => Tag.setCoverId(tagId, cId, manual = true))
          r.complete(SuccessResponse)
        case None => r.complete(ErrorResponse.NotFound)
      }
  }


  private def processGetTag: Receive = {
    case r @ GetTag(tagId) =>
      Tag.find(tagId) match {
        case Some(t) => r.complete(GetTagResponse(t))
        case None => r.complete(ErrorResponse.NotFound)
      }
  }


  private def processGetRecentTags: Receive = {
    case r @ GetRecentTags(userId) =>
      val tags = Tag.getRecentTags(userId, r.offset, r.limit)
      r.complete(GetTagsResponse(tags))
  }


  private def processCreateTag: Receive = {
    case r @CreateTag(name) =>
      val ownerId = r.session.get.userId
      val tag = DB localTx { implicit s =>
        createTag(name, ownerId)
      }
      r.complete(CreateTagResponse(tag))
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
