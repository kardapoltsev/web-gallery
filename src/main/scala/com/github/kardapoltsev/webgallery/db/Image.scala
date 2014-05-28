package com.github.kardapoltsev.webgallery.db

import spray.json.DefaultJsonProtocol
import org.mybatis.scala.mapping._
import java.util.{UUID, Date}

/**
 * Created by alexey on 5/27/14.
 */
case class Metadata(cameraModel: String, creationTime: Date)


case class Tag(id: Long, name: String)
object Tag {

  val selectTags = new SelectListBy[Long, Tag] {
    def xsql =
      <xsql>
        select * from tags t where t.image_id = #{{imageId}}
      </xsql>
  }

  def bind = Seq(selectTags)
}


/**
 * Heart of gallery
 * @param id unique id used in database
 * @param filename unique file name used to store and find on filesystem
 * @param name uploaded original file name
 * @param tags user defined tags
 * @param metadata
 */
case class Image(
    name: String, tags: Seq[Tag], metadata: Option[Metadata], filename: String = UUID.randomUUID().toString, id: Long = 0L)

object Image {
  val selectSql = "select * from images i"


  val imageResultMap = new ResultMap[Image] {
    id(property = "id", column = "id")
    result(property = "filename", column = "filename")
    result(property = "name", column = "name")
    association[Metadata](property = "metadata", column = "metadata_id", select = Tag.selectTags) //TODO: fix
    collection[Tag](property = "tags", column = "id", select = Tag.selectTags)
  }


  val insertImage = new Insert[Image] {
    def xsql =
      <xsql>
        insert into images (name, filename, metadata_id) values (#{{name}}, #{{filename}}, 0)
      </xsql>
  }


  val selectById = new SelectOneBy[Long, Image] {
    resultMap = imageResultMap
    def xsql =
      <xsql>
        {selectSql} where i.id = #{{id}}"
      </xsql>
  }


  val selectAll = new SelectList[Image] {
    resultMap = imageResultMap
    def xsql = <xsql>{selectSql}</xsql>
  }


  def bind = Seq(selectById, selectAll, insertImage)
}
