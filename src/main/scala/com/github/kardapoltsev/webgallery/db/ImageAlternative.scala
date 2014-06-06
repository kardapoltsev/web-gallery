package com.github.kardapoltsev.webgallery.db

import com.github.kardapoltsev.webgallery.db.Image.ImageId
import org.mybatis.scala.mapping._
import org.mybatis.scala.mapping.Binding._

/**
 * Created by alexey on 6/6/14.
 */

case class ImageAlternative(imageId: ImageId, filename: String, transform: TransformImageParams, id: Int = 0)

case class TransformImageParams(width: Int, height: Int, crop: Boolean)

object ImageAlternative {
  val transformImageParamsMap = new ResultMap[TransformImageParams]{
    arg(column = "width", javaType = T[Int])
    arg(column = "height", javaType = T[Int])
    arg(column = "crop", javaType = T[Boolean])
  }


  val imageAlternativeMap = new ResultMap[ImageAlternative]{
    arg(column = "image_id", javaType = T[ImageId])
    arg(column = "filename", javaType = T[String])
    arg(javaType = T[TransformImageParams], resultMap = transformImageParamsMap)
    idArg(column = "id", javaType = T[Int])
  }
}
