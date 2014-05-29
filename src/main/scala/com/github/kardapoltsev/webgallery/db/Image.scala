package com.github.kardapoltsev.webgallery.db

import org.mybatis.scala.mapping._
import org.mybatis.scala.mapping.Binding._
import java.util.{UUID, Date}



/**
 * Heart of gallery
 * @param id unique id used in database
 * @param filename unique file name used to store and find on filesystem
 * @param name uploaded original file name
 * @param tags user defined tags
 * @param mdata
 */
case class Image(
    name: String,
    tags: Seq[Tag],
    private val mdata: Metadata,
    filename: String = UUID.randomUUID().toString,
    id: Int = 0) {

  //only for insert statement
  def metadata: Option[Metadata] = Option(mdata)
  def getMetadataId = metadata.map(_.id).getOrElse(null)
}

object Image {
  val selectSql = "select * from images i"


  val imageResultMap = new ResultMap[Image] {
    arg(column = "name", javaType = T[String])
    arg(column = "id", select = Tag.selectTags, javaType = T[Seq[Tag]])
    arg(column = "metadata_id", select = Metadata.selectById, javaType = T[Metadata])
    arg(column = "filename", javaType = T[String])
    idArg(column = "id", javaType = T[Int])
  }


  val insertImage = new Insert[Image] {
    keyGenerator = JdbcGeneratedKey("id", "id")

    def xsql =
      <xsql>
        insert into images (name, filename, metadata_id) values (#{{name}}, #{{filename}}, #{{metadataId}})
      </xsql>
  }


  val selectById = new SelectOneBy[Int, Image] {
    resultMap = imageResultMap
    def xsql =
      <xsql>
        {selectSql} where i.id = #{{id}}
      </xsql>
  }


  val deleteById = new Delete[Int] {
    def xsql =
      <xsql>
        delete from images where id = {? ("imageId")}
      </xsql>
  }


  val selectAll = new SelectList[Image] {
    resultMap = imageResultMap
    def xsql = <xsql>{selectSql}</xsql>
  }


  def bind = Seq(selectById, deleteById, selectAll, insertImage)
}
