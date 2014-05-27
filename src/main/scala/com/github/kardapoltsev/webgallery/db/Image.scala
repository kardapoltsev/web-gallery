package com.github.kardapoltsev.webgallery.db

import spray.json.DefaultJsonProtocol

/**
 * Created by alexey on 5/27/14.
 */
case class Metadata(cameraModel: String, tags: Seq[String])
object Metadata extends DefaultJsonProtocol {
  implicit val _ = jsonFormat(Metadata.apply, "cameraModel", "tags")
}


case class Image(filename: String, metadata: Metadata)

object Image extends DefaultJsonProtocol {
  implicit val _ = jsonFormat(Image.apply, "filename", "metadata")
}
