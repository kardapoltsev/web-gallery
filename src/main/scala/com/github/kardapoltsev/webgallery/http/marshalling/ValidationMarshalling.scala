package com.github.kardapoltsev.webgallery.http.marshalling

import com.github.kardapoltsev.webgallery.StatsManager.GetStats
import com.github.kardapoltsev.webgallery.ValidationManager.ValidateLogin

/**
 * Created by alexey on 8/26/14.
 */
trait ValidationMarshalling { this: WebGalleryMarshalling =>

  implicit val validationUM = unmarshallerFrom {
    authId: String => ValidateLogin(authId)
  }

}
