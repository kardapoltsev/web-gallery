package com.github.kardapoltsev.webgallery.db.gen

import scalikejdbc._

case class Alternative(
  id: Int,
  imageId: Int,
  filename: String,
  width: Option[Int],
  height: Option[Int],
  scaleType: String)

object Alternative extends SQLSyntaxSupport[Alternative] {

  override val tableName = "alternative"

  override val columns = Seq("id", "image_id", "filename", "width", "height", "scale_type")

  def apply(a: SyntaxProvider[Alternative])(rs: WrappedResultSet): Alternative = apply(a.resultName)(rs)
  def apply(a: ResultName[Alternative])(rs: WrappedResultSet): Alternative = new Alternative(
    id = rs.get(a.id),
    imageId = rs.get(a.imageId),
    filename = rs.get(a.filename),
    width = rs.get(a.width),
    height = rs.get(a.height),
    scaleType = rs.get(a.scaleType)
  )

  val a = Alternative.syntax("a")

  override val autoSession = AutoSession

  def find(id: Int)(implicit session: DBSession = autoSession): Option[Alternative] = {
    withSQL {
      select.from(Alternative as a).where.eq(a.id, id)
    }.map(Alternative(a.resultName)).single.apply()
  }

  def create(
    imageId: Int,
    filename: String,
    width: Option[Int],
    height: Option[Int],
    scaleType: String)(implicit session: DBSession): Alternative = {
    val generatedKey = withSQL {
      insert.into(Alternative).columns(
        column.imageId,
        column.filename,
        column.width,
        column.height,
        column.scaleType
      ).values(
          imageId,
          filename,
          width,
          height,
          scaleType
        )
    }.updateAndReturnGeneratedKey.apply()

    Alternative(
      id = generatedKey.toInt,
      imageId = imageId,
      filename = filename,
      width = width,
      height = height,
      scaleType = scaleType)
  }

  def destroy(entity: Alternative)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(Alternative).where.eq(column.id, entity.id) }.update().apply()
  }

}
