package com.github.kardapoltsev.webgallery.http.marshalling

import com.github.kardapoltsev.webgallery.CommentManager.{ DeleteComment, GetComments, AddComment }
import com.github.kardapoltsev.webgallery.db.{ CommentId, ImageId }
import spray.json.RootJsonFormat

/**
 * Created by alexey on 8/26/14.
 */
trait CommentsMarshalling { this: WebGalleryMarshalling =>

  case class AddCommentBody(text: String, parentCommentId: Option[CommentId])
  implicit val addCommentBodyJF: RootJsonFormat[AddCommentBody] = jsonFormat2(AddCommentBody.apply)
  //  implicit val addCommentBodyUM = sprayJsonUnmarshaller[AddCommentBody]
  implicit val addCommentUM = compositeUnmarshallerFrom {
    (body: AddCommentBody, imageId: ImageId) => AddComment(imageId, body.text, body.parentCommentId)
  }

  implicit val getCommentUM = unmarshallerFrom {
    (imageId: ImageId, offset: Option[Int], limit: Option[Int]) =>
      GetComments(imageId).withPagination(offset, limit)
  }

  implicit val deleteCommentUM = unmarshallerFrom {
    (commentId: CommentId) => DeleteComment(commentId)
  }

}
