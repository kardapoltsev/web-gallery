package com.github.kardapoltsev.webgallery

import com.github.kardapoltsev.webgallery.processing.{ScaleType, SpecificSize}
import scala.language.implicitConversions

/**
 * Created by alexey on 6/8/14.
 */
package object db {
  type Image = gen.Image
  type Tag = gen.Tag
  type Alternative = gen.Alternative
  type Metadata = gen.Metadata
  type ImageTag = gen.ImageTag

  implicit def alternativeToGen(o: Alternative.type) = gen.Alternative
  implicit def tagToGen(o: Tag.type) = gen.Tag
  implicit def imageToGen(o: Image.type) = gen.Image
  implicit def imageTagToGen(o: ImageTag.type) = gen.ImageTag

  implicit class RichAlternative(self: Alternative) {
    def size: SpecificSize = SpecificSize(self.width, self.height, ScaleType.withName(self.scaleType))
  }
}
