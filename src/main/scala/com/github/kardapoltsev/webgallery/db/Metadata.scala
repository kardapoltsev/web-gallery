package com.github.kardapoltsev.webgallery.db

import org.mybatis.scala.mapping._
import java.util.Date
import org.apache.ibatis.`type`.DateTypeHandler



/**
 * Created by alexey on 5/27/14.
 */
case class Metadata(cameraModel: String, creationTime: Date, id: Int = 0)


object Metadata {

  val selectSql = "select * from metadata m"

  val metadataMap = new ResultMap[Metadata]{
    arg(column = "camera_model", javaType = T[String])
    arg(column = "creation_time", javaType = T[Date])
    idArg(column = "id", javaType = T[Int])
  }

  val selectById = new SelectOneBy[Long, Metadata] {
    resultMap = metadataMap
    def xsql =
      <xsql>
        {selectSql} where m.id = #{{metadataId}}
      </xsql>
  }


  def bind = Seq(selectById)
}
