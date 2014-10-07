package com.github.kardapoltsev.webgallery.http.marshalling

import com.github.kardapoltsev.webgallery.db.{ImageId, TagId}
import com.github.kardapoltsev.webgallery.processing.{OptionalSize, ScaleType}
import shapeless._
import spray.http.MultipartFormData



/**
 * Created by alexey on 8/26/14.
 */
trait ImagesMarshalling { this: WebGalleryMarshalling =>
  import com.github.kardapoltsev.webgallery.ImageManager._
  import com.github.kardapoltsev.webgallery.ImageHolder._

  implicit val updateImageUM: FromRequestWithParamsUnmarshaller[Int :: HNil, UpdateImage] =
    compositeUnmarshallerFrom {
      (body: UpdateImageParams, imageId: ImageId) => UpdateImage(imageId, body)
    }


  implicit val getImageUM = unmarshallerFrom {
    imageId: ImageId => GetImage(imageId)
  }


  implicit val getByTagUM = unmarshallerFrom {
    (tagId: TagId, offset: Option[Int], limit: Option[Int]) => withPagination(GetByTag(tagId), offset, limit)
  }


  implicit val transformImageUM = unmarshallerFrom {
    (imageId : ImageId, width: Option[Int], height: Option[Int], scale: String) =>
      val scaleType = ScaleType.withName(scale)
      TransformImageRequest(imageId, OptionalSize(width, height, scaleType))
  }


  implicit val uploadImageUM = unmarshallerFrom {
    form: MultipartFormData =>
      val filePart = form.fields.head
      val filename = filePart.filename.get
      UploadImageRequest(filename, filePart.entity.data.toByteArray)
  }


  implicit val getPopularImagesUM = unmarshallerFrom {
    (offset: Option[Int], limit: Option[Int]) => withPagination(GetPopularImages, offset, limit)
  }

}
