package com.github.kardapoltsev.webgallery.db

import org.mybatis.scala.mapping._
import org.mybatis.scala.mapping.Binding._



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
    filename: String,
    id: Int = 0) {

  //only for insert statement
  def metadata: Option[Metadata] = Option(mdata)
  def getMetadataId = metadata.map(_.id).getOrElse(null)
}

object Image {
  val selectSql = "select * from images i"


  val imageMap = new ResultMap[Image] {
    arg(column = "name", javaType = T[String])
    arg(column = "id", select = Tag.getImageTags, javaType = T[Seq[Tag]])
    arg(column = "metadata_id", select = Metadata.selectById, javaType = T[Metadata])
    arg(column = "filename", javaType = T[String])
    idArg(column = "id", javaType = T[Int])
  }


  val insert = new Insert[Image] {
    keyGenerator = JdbcGeneratedKey("id", "id")

    def xsql =
      <xsql>
        insert into images (name, filename, metadata_id) values (#{{name}}, #{{filename}}, #{{metadataId}})
      </xsql>
  }


  val addTag = new Insert[ImagesTags]() {
    def xsql =
      <xsql>
        insert into images_tags(image_id, tag_id) values ({?("imageId")}, {?("tagId")})
      </xsql>
  }


  val getById = new SelectOneBy[Int, Image] {
    resultMap = imageMap
    def xsql =
      <xsql>
        {selectSql} where i.id = #{{id}}
      </xsql>
  }


  val getByTag = new SelectListBy[String, Image]() {
    resultMap = imageMap
    def xsql =
      <xsql>
        {selectSql}
        join images_tags it on it.image_id = i.id
        join tags t on t.id = it.tag_id
        where t.name = {?("tag")}
      </xsql>
  }



  val deleteById = new Delete[Int] {
    def xsql =
      <xsql>
        delete from images where id = {? ("imageId")}
      </xsql>
  }


  def bind = Seq(getById, getByTag, deleteById, insert, addTag)
}

case class ImagesTags(imageId: Int, tagId: Int)
