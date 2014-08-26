package com.github.kardapoltsev.webgallery

import akka.actor.Actor
import com.github.kardapoltsev.webgallery.db.{Image, ImageId}
import com.github.kardapoltsev.webgallery.http.ErrorResponse



/**
 * Created by alexey on 8/26/14.
 */
trait ImageHelper { this: Actor =>
  protected def withImage(imageId: ImageId)(action: Image => Any): Unit = {
    Image.find(imageId) match {
      case Some(image) => action(image)
      case None => sender() ! ErrorResponse.NotFound
    }
  }
}
