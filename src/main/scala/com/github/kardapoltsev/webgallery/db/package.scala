package com.github.kardapoltsev.webgallery

import com.github.kardapoltsev.webgallery.processing.{ScaleType, SpecificSize}
import scala.language.implicitConversions
import spray.json.DefaultJsonProtocol



/**
 * Created by alexey on 6/8/14.
 */
package object db extends DefaultJsonProtocol {
  type Image = gen.Image
  type Tag = gen.Tag
  type Alternative = gen.Alternative
  type Metadata = gen.Metadata
  type ImageTag = gen.ImageTag
  type User = gen.User
  type Credentials = gen.Credentials
  type UserId = Int

  implicit def alternativeToGen(o: Alternative.type) = gen.Alternative
  implicit def tagToGen(o: Tag.type) = gen.Tag
  implicit def imageToGen(o: Image.type) = gen.Image
  implicit def imageTagToGen(o: ImageTag.type) = gen.ImageTag
  implicit def metadataToGen(o: Metadata.type) = gen.Metadata
  implicit def userToGen(o: User.type) = gen.User
  implicit def credentialsToGen(o: Credentials.type) = gen.Credentials

  implicit class RichAlternative(self: Alternative) {
    def size: SpecificSize = SpecificSize(self.width, self.height, ScaleType.withName(self.scaleType))
  }

  implicit val TagJF = jsonFormat2(gen.Tag.apply)
}
