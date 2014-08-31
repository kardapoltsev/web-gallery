package com.github.kardapoltsev.webgallery.http


import com.github.kardapoltsev.webgallery.AclManager.{GetGrantees, RevokeAccess, GrantAccess}
import com.github.kardapoltsev.webgallery.CommentManager.{GetComments, AddComment}
import com.github.kardapoltsev.webgallery.ImageManager.{UploadImageRequest, TransformImageRequest}
import com.github.kardapoltsev.webgallery.processing.{OptionalSize, ScaleType}
import spray.http._
import spray.httpx.unmarshalling.FromRequestUnmarshaller
import spray.json._
import shapeless._
import com.github.kardapoltsev.webgallery.UserManager._



/**
 * Created by alexey on 6/2/14.
 */
package object marshalling extends DefaultJsonProtocol with WebGalleryMarshalling with TagsMarshalling
  with LikesMarshalling {
  import com.github.kardapoltsev.webgallery.Database._
  import com.github.kardapoltsev.webgallery.db._


  implicit val transformImageUM = unmarshallerFrom {
    (imageId : ImageId, width: Option[Int], height: Option[Int], scale: String) =>
      val scaleType = ScaleType.withName(scale)
      TransformImageRequest(imageId, OptionalSize(width, height, scaleType))
  }


  implicit val uploadImageUM = unmarshallerFrom {
    form: MultipartFormData =>
      val filePart = form.fields.head
      val filename = filePart.headers.find(h => h.is("content-disposition")).get.value.split("filename=").
          last.takeWhile(ch => ch != ';')
      UploadImageRequest(filename, filePart.entity.data.toByteArray)
  }


  implicit val getCurrentUserUM = unmarshallerFrom {
    () => GetCurrentUser()
  }


  case class AddCommentBody(text: String, parentCommentId: Option[CommentId])
  implicit val _ = jsonFormat2(AddCommentBody.apply)
  implicit val addCommentUM = compositeUnmarshallerFrom {
    (body: AddCommentBody, imageId: ImageId) => AddComment(imageId, body.text, body.parentCommentId)
  }


  implicit val getCommentUM = unmarshallerFrom {
    (imageId: ImageId, offset: Option[Int], limit: Option[Int]) =>
      withPagination(GetComments(imageId), offset, limit)
  }


  implicit val searchUsersUM = unmarshallerFrom {
    query: String => SearchUsers(query)
  }


  implicit val getUserUM = unmarshallerFrom {
    userId: UserId => GetUser(userId)
  }


  implicit val vkAuthUM = unmarshallerFrom {
    code: String => VKAuth(code)
  }


//  implicit val registerUserUM: FromRequestUnmarshaller[RegisterUser] = unmarshallerFrom(RegisterUser.registerUserJF)


  implicit val updateImageUM: FromRequestWithParamsUnmarshaller[Int :: HNil, UpdateImage] =
    compositeUnmarshallerFrom {
      (body: UpdateImageParams, imageId: ImageId) => UpdateImage(imageId, body)
    }


  implicit val getImageUM = unmarshallerFrom {
    imageId: ImageId => GetImage(imageId)
  }


  implicit val getByTagUM = unmarshallerFrom {
    tagId: TagId => GetByTag(tagId)
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
