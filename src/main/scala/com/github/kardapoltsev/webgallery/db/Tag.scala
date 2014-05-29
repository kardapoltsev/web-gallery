package com.github.kardapoltsev.webgallery.db

import org.mybatis.scala.mapping._
import org.mybatis.scala.mapping.Binding._


/**
 * Created by alexey on 5/29/14.
 */
case class Tag(name: String, id: Int = 0)
object Tag {

  val selectSql = "select t.* from tags t"


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


  val selectByName = new SelectOneBy[String, Tag] {
    resultMap = tagMap
    def xsql = <xsql>{selectSql} where name = {?("tagName")}</xsql>
  }


  val deleteById = new Delete[Int] {
    def xsql = <xsql>delete from tags where id = {?("tagId")}</xsql>
  }



  val getImageTags = new SelectListBy[Int, Tag] {
    resultMap = tagMap
    def xsql =
      <xsql>
        {selectSql}
        join images_tags it on it.tag_id = t.id
        where it.image_id = #{{imageId}}
      </xsql>
  }


  /**
   * Query for all tags in database
   */
  val getTags = new SelectList[Tag]() {
    resultMap = tagMap
    def xsql =
      <xsql>
        select t.* from tags t
      </xsql>
  }


  def deleteAll = new Delete[Nothing]() {
    def xsql = <xsql>delete from tags</xsql>
  }



  def bind = Seq(insert, selectById, selectByName, deleteById, getImageTags, getTags, deleteAll)
}
