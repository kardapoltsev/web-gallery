package com.github.kardapoltsev.webgallery.db.gen

import scalikejdbc._

case class ImageTag(
  imageId: Int, 
  tagId: Int) {

  def save()(implicit session: DBSession = ImageTag.autoSession): ImageTag = ImageTag.save(this)(session)

  def destroy()(implicit session: DBSession = ImageTag.autoSession): Unit = ImageTag.destroy(this)(session)

}
      

object ImageTag extends SQLSyntaxSupport[ImageTag] {

  override val tableName = "image_tag"

  override val columns = Seq("image_id", "tag_id")

  def apply(it: SyntaxProvider[ImageTag])(rs: WrappedResultSet): ImageTag = apply(it.resultName)(rs)
  def apply(it: ResultName[ImageTag])(rs: WrappedResultSet): ImageTag = new ImageTag(
    imageId = rs.get(it.imageId),
    tagId = rs.get(it.tagId)
  )
      
  val it = ImageTag.syntax("it")

  override val autoSession = AutoSession

  def find(imageId: Int, tagId: Int)(implicit session: DBSession = autoSession): Option[ImageTag] = {
    withSQL {
      select.from(ImageTag as it).where.eq(it.imageId, imageId).and.eq(it.tagId, tagId)
    }.map(ImageTag(it.resultName)).single.apply()
  }
          
  def findAll()(implicit session: DBSession = autoSession): List[ImageTag] = {
    withSQL(select.from(ImageTag as it)).map(ImageTag(it.resultName)).list.apply()
  }
          
  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls"count(1)").from(ImageTag as it)).map(rs => rs.long(1)).single.apply().get
  }
          
  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[ImageTag] = {
    withSQL { 
      select.from(ImageTag as it).where.append(sqls"${where}")
    }.map(ImageTag(it.resultName)).list.apply()
  }
      
  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL { 
      select(sqls"count(1)").from(ImageTag as it).where.append(sqls"${where}")
    }.map(_.long(1)).single.apply().get
  }
      
  def create(
    imageId: Int,
    tagId: Int)(implicit session: DBSession = autoSession): ImageTag = {
    withSQL {
      insert.into(ImageTag).columns(
        column.imageId,
        column.tagId
      ).values(
        imageId,
        tagId
      )
    }.update.apply()

    ImageTag(
      imageId = imageId,
      tagId = tagId)
  }

  def save(entity: ImageTag)(implicit session: DBSession = autoSession): ImageTag = {
    withSQL {
      update(ImageTag).set(
        column.imageId -> entity.imageId,
        column.tagId -> entity.tagId
      ).where.eq(column.imageId, entity.imageId).and.eq(column.tagId, entity.tagId)
    }.update.apply()
    entity
  }
        
  def destroy(entity: ImageTag)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(ImageTag).where.eq(column.imageId, entity.imageId).and.eq(column.tagId, entity.tagId) }.update.apply()
  }
        
}
