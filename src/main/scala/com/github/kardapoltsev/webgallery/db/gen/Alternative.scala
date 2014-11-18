package com.github.kardapoltsev.webgallery.db.gen

import scalikejdbc._

case class Alternative(
    id: Int,
    imageId: Int,
    filename: String,
    width: Option[Int],
    height: Option[Int],
    scaleType: String) {

  def save()(implicit session: DBSession = Alternative.autoSession): Alternative = Alternative.save(this)(session)

  def destroy()(implicit session: DBSession = Alternative.autoSession): Unit = Alternative.destroy(this)(session)

}

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

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[Alternative] = {
    withSQL {
      select.from(Alternative as a).where.append(sqls"${where}")
    }.map(Alternative(a.resultName)).list.apply()
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

  def save(entity: Alternative)(implicit session: DBSession = autoSession): Alternative = {
    withSQL {
      update(Alternative).set(
        column.id -> entity.id,
        column.imageId -> entity.imageId,
        column.filename -> entity.filename,
        column.width -> entity.width,
        column.height -> entity.height,
        column.scaleType -> entity.scaleType
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def destroy(entity: Alternative)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(Alternative).where.eq(column.id, entity.id) }.update.apply()
  }

}
