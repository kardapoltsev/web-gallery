package com.github.kardapoltsev.webgallery.es

import com.github.kardapoltsev.webgallery.db.{ ImageMetadata, User, Tag, Image }

/**
 * Created by alexey on 9/9/14.
 */
trait GalleryEvent {

}

trait ImageEvent extends GalleryEvent {
  def image: Image
}
case class ImageCreated(image: Image, meta: Option[ImageMetadata]) extends ImageEvent
case class ImageTagged(image: Image, tag: Tag) extends ImageEvent
case class ImageUntagged(image: Image, tag: Tag) extends ImageEvent

trait UserEvent extends GalleryEvent {
  def user: User
}
case class UserCreated(user: User) extends UserEvent
