package com.github.kardapoltsev.webgallery.db

import org.mybatis.scala.mapping._


/**
 * Created by alexey on 5/29/14.
 */
case class Tag(name: String, id: Int = 0)
object Tag {

  val selectSql = "select * from tags t"

  val tagMap = new ResultMap[Tag]{
    arg(column = "name", javaType = T[String])
    idArg(column = "id", javaType = T[Int])
  }

  val selectTags = new SelectListBy[Long, Tag] {
    resultMap = tagMap
    def xsql =
      <xsql>
        {selectSql}
        join images_tags it on it.tag_id = t.id
        where it.image_id = #{{imageId}}
      </xsql>
  }

  def bind = Seq(selectTags)
}
