package com.github.kardapoltsev.webgallery.dto


import com.github.kardapoltsev.webgallery.db.{Tag, Image}
import spray.json.DefaultJsonProtocol


/**
 * Created by alexey on 6/8/14.
 */
case class ImageInfo(id: Int, name: String, filename: String, tags: Seq[String])
object ImageInfo extends DefaultJsonProtocol {
  def apply(image: Image, tags: Seq[Tag]): ImageInfo = {
    ImageInfo(image.id, image.name, image.filename, tags.map(_.name))
  }

  implicit val _ = jsonFormat4(ImageInfo.apply)
}
