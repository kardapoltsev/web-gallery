package com.github.kardapoltsev.webgallery


import akka.actor.{ActorLogging, Actor}
import com.github.kardapoltsev.webgallery.db._
import com.github.kardapoltsev.webgallery.http.{Pagination, SuccessResponse, ApiRequest, AuthorizedRequest}
import com.github.kardapoltsev.webgallery.routing.{TagsManagerRequest}
import scalikejdbc.DB
import spray.json.DefaultJsonProtocol



/**
 * Created by alexey on 8/26/14.
 */
class TagsManager extends Actor with ActorLogging {
  import TagsManager._

  def receive: Receive = processGetRecentTags orElse {

    case r: CreateTag => sender() ! CreateTagResponse(createTag(r.session.get.userId, r.name))

    case GetTags(userId) => sender() ! GetTagsResponse(getTagsByUserId(userId))

    case GetImageTags(imageId) => sender() ! GetTagsResponse(getTags(imageId))

    case SearchTags(query) => sender() ! GetTagsResponse(searchTags(query))

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
  import db.tagJF

  case class GetTags(userId: UserId) extends AuthorizedRequest with TagsManagerRequest


  case class GetRecentTags(userId: UserId) extends AuthorizedRequest with TagsManagerRequest with Pagination


  case class GetImageTags(imageId: Int) extends ApiRequest with TagsManagerRequest
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

}
