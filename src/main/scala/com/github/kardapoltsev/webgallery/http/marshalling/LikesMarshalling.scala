package com.github.kardapoltsev.webgallery.http.marshalling

import com.github.kardapoltsev.webgallery.db.{ImageId, UserId}



/**
 * Created by alexey on 8/26/14.
 */
trait LikesMarshalling { this: WebGalleryMarshalling =>
  import com.github.kardapoltsev.webgallery.LikeManager._


  implicit val likeImageUM = unmarshallerFrom {
    imageId: ImageId => LikeImage(imageId)
  }

  implicit val unlikeImageUM = unmarshallerFrom {
    imageId: ImageId => UnlikeImage(imageId)
  }

}
