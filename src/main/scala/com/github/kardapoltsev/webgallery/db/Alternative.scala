package com.github.kardapoltsev.webgallery.db

import com.github.kardapoltsev.webgallery.db.Image.ImageId
import org.mybatis.scala.mapping._
import org.mybatis.scala.mapping.Binding._

/**
 * Created by alexey on 6/6/14.
 */

case class Alternative(imageId: ImageId, filename: String, transform: TransformImageParams, id: Int = 0)

case class TransformImageParams(width: Int, height: Int, crop: Boolean)

object Alternative {
  type AlternativeId = Int
  
  val selectSql = "select * from image_alternatives ia"


  val transformImageParamsMap = new ResultMap[TransformImageParams]{
    arg(column = "width", javaType = T[Int])
    arg(column = "height", javaType = T[Int])
    arg(column = "crop", javaType = T[Boolean])
  }


  val imageAlternativeMap = new ResultMap[Alternative]{
    arg(column = "image_id", javaType = T[ImageId])
    arg(column = "filename", javaType = T[String])
    arg(javaType = T[TransformImageParams], resultMap = transformImageParamsMap)
    idArg(column = "id", javaType = T[Int])
  }


  val create = new Insert[Alternative] {
    keyGenerator = JdbcGeneratedKey("id", "id")

    def xsql =
      <xsql>
        insert into image_alternatives (image_id, filename, width, height, crop)
        values
        ({?("imageId")}, {?("filename")}, {?("transform.width")}, {?("transform.height")}, {?("transform.crop")})
      </xsql>
  }


  val getById = new SelectOneBy[Int, Alternative] {
    resultMap = imageAlternativeMap
    def xsql =
      <xsql>
        {selectSql} where ia.id = #{{id}}
      </xsql>
  }


  //TODO: specific class for sql param
  val find = new SelectOneBy[Alternative, Alternative] {
    resultMap = imageAlternativeMap

    def xsql =
      <xsql>
        {selectSql}
        where ia.image_id = {?("imageId")}
          and ia.width >= {?("transform.width")}
          and ia.height >= {?("transform.height")}
          and crop = false
          order by ia.width, ia.height desc limit 1
      </xsql>
  }



  def bind = Seq(create, getById, find)
}

