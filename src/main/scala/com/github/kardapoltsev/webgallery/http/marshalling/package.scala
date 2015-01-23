package com.github.kardapoltsev.webgallery.http

import com.github.kardapoltsev.webgallery.acl.AclManager
import AclManager.{ GetGrantees, RevokeAccess, GrantAccess }
import com.github.kardapoltsev.webgallery.CommentManager.{ GetComments, AddComment }
import shapeless._
import com.github.kardapoltsev.webgallery.UserManager._
import spray.json.{ RootJsonFormat, JsonFormat, DefaultJsonProtocol }

/**
 * Created by alexey on 6/2/14.
 */
package object marshalling extends DefaultJsonProtocol with WebGalleryMarshalling with TagsMarshalling
    with LikesMarshalling with ImagesMarshalling with CommentsMarshalling with StatsMarshalling {
  import com.github.kardapoltsev.webgallery.db._

  implicit val getCurrentUserUM = unmarshallerFrom {
    () => GetCurrentUser()
  }

  implicit val searchUsersUM = unmarshallerFrom {
    (query: String, offset: Option[Int], limit: Option[Int]) => SearchUsers(query).withPagination(offset, limit)
  }

  implicit val getUserUM = unmarshallerFrom {
    userId: UserId => GetUser(userId)
  }

  implicit val vkAuthUM = unmarshallerFrom {
    code: String => VKAuth(code)
  }

  implicit val grantAccessUM = compositeUnmarshallerFrom {
    (users: Seq[UserId], tagId: TagId) => GrantAccess(tagId, users)
  }

  implicit val revokeAccessUM = compositeUnmarshallerFrom {
    (users: Seq[UserId], tagId: TagId) => RevokeAccess(tagId, users)
  }

  implicit val getGranteesUM = unmarshallerFrom {
    tagId: TagId => GetGrantees(tagId)
  }

}
