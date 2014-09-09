package com.github.kardapoltsev.webgallery.http.marshalling

import com.github.kardapoltsev.webgallery.db.{TagId, UserId}
import com.github.kardapoltsev.webgallery.tags.TagsManager



/**
 * Created by alexey on 8/26/14.
 */
trait TagsMarshalling { this: WebGalleryMarshalling =>
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
    query: String => SearchTags(query)
  }

}
