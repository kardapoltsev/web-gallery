package com.github.kardapoltsev.webgallery.db

import org.mybatis.scala.mapping._
import org.mybatis.scala.mapping.Binding._


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


  val insert = new Insert[Tag] {
    keyGenerator = JdbcGeneratedKey("id", "id")
    def xsql = <xsql>insert into tags (name) values ({?("name")}) </xsql>
  }


  val selectById = new SelectOneBy[Int, Tag] {
    resultMap = tagMap
    def xsql = <xsql>{selectSql} where id = {?("tagId")}</xsql>
  }


  val selectByName = new SelectOneBy[Int, Tag] {
    resultMap = tagMap
    def xsql = <xsql>{selectSql} where id = {?("tagId")}</xsql>
  }


  val deleteById = new Delete[Int] {
    def xsql = <xsql>delete from tags where id = {?("tagId")}</xsql>
  }



  val selectTags = new SelectListBy[Int, Tag] {
    resultMap = tagMap
    def xsql =
      <xsql>
        {selectSql}
        join images_tags it on it.tag_id = t.id
        where it.image_id = #{{imageId}}
      </xsql>
  }


  def bind = Seq(insert, selectById, deleteById, selectTags)
}
