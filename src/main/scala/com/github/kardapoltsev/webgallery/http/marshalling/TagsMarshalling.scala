package com.github.kardapoltsev.webgallery.http.marshalling

import com.github.kardapoltsev.webgallery.db.{TagId, UserId}
import com.github.kardapoltsev.webgallery.tags.TagsManager
import spray.json.DefaultJsonProtocol



/**
 * Created by alexey on 8/26/14.
 */
trait TagsMarshalling extends DefaultJsonProtocol { this: WebGalleryMarshalling =>
  import TagsManager._

  implicit val getTagUM = unmarshallerFrom {
    tagId: TagId => GetTag(tagId)
  }

  implicit val getTagsUM = unmarshallerFrom {
    userId: UserId => GetTags(userId)
  }

  implicit val getRecentTagsUM = unmarshallerFrom {
    (userId: UserId, offset: Option[Int], limit: Option[Int]) =>
      withPagination(GetRecentTags(userId), offset, limit)
  }

  implicit val searchTagsUM = unmarshallerFrom {
    (query: String, offset: Option[Int], limit: Option[Int]) => withPagination(SearchTags(query), offset, limit)
  }

  case class UpdateTagBody(name: Option[String], coverId: Option[TagId])
  object UpdateTagBody {
    implicit val _ = jsonFormat2(UpdateTagBody.apply)
  }
  implicit val updateTagUM = compositeUnmarshallerFrom {
    (body: UpdateTagBody, tagId: TagId) => UpdateTag(tagId, body.name, body.coverId)
  }

}
