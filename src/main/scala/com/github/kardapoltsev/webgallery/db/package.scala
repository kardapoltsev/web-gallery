package com.github.kardapoltsev.webgallery

import com.github.kardapoltsev.webgallery.processing.{ScaleType, SpecificSize}
import scala.language.implicitConversions
import spray.json.{JsValue, JsString, DefaultJsonProtocol}
import com.github.kardapoltsev.webgallery.db.AuthType.AuthType
import org.joda.time.DateTime



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
  type Session = gen.Session
  type Acl = gen.Acl
  type UserId = Int
  type SessionId = Int

  implicit def alternativeToGen(o: Alternative.type) = gen.Alternative
  implicit def tagToGen(o: Tag.type) = gen.Tag
  implicit def imageToGen(o: Image.type) = gen.Image
  implicit def imageTagToGen(o: ImageTag.type) = gen.ImageTag
  implicit def metadataToGen(o: Metadata.type) = gen.Metadata
  implicit def userToGen(o: User.type) = gen.User
  implicit def credentialsToGen(o: Credentials.type) = gen.Credentials
  implicit def sessionToGen(o: Session.type) = gen.Session
  implicit def aclToGen(o: Acl.type) = gen.Acl


  implicit class RichAlternative(self: Alternative) {
    def size: SpecificSize = SpecificSize(self.width, self.height, ScaleType.withName(self.scaleType))
  }


  implicit object DateTimeFormat extends scala.AnyRef with spray.json.JsonFormat[DateTime] {
    def write(x: DateTime): JsString =
      JsString(x.toString())
    def read(value: JsValue): DateTime = value match {
      case JsString(v) => DateTime.parse(v)
      case x => spray.json.deserializationError("Expected DateTime as JsString, but got " + x)
    }
  }


  implicit val tagJF = jsonFormat2(gen.Tag.apply)
  implicit val userJF = jsonFormat3(gen.User.apply)
}
