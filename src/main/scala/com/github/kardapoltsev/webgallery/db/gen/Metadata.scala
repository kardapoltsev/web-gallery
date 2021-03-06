package com.github.kardapoltsev.webgallery.db.gen

import scalikejdbc._
import org.joda.time.{ DateTime }

case class Metadata(
  id: Int,
  imageId: Int,
  cameraModel: Option[String] = None,
  creationTime: Option[DateTime] = None,
  iso: Option[Int] = None,
  lensModel: Option[String] = None)

object Metadata extends SQLSyntaxSupport[Metadata] {

  override val tableName = "metadata"

  override val columns = Seq("id", "image_id", "camera_model", "iso", "lens_model", "creation_time")

  def apply(m: SyntaxProvider[Metadata])(rs: WrappedResultSet): Metadata = apply(m.resultName)(rs)
  def apply(m: ResultName[Metadata])(rs: WrappedResultSet): Metadata = new Metadata(
    id = rs.get(m.id),
    imageId = rs.get(m.imageId),
    cameraModel = rs.get(m.cameraModel),
    creationTime = rs.get(m.creationTime),
    iso = rs.get(m.iso),
    lensModel = rs.get(m.lensModel)
  )

  val m = Metadata.syntax("m")

  override val autoSession = AutoSession

  def find(id: Int)(implicit session: DBSession = autoSession): Option[Metadata] = {
    withSQL {
      select.from(Metadata as m).where.eq(m.id, id)
    }.map(Metadata(m.resultName)).single.apply()
  }

  def create(
    imageId: Int,
    cameraModel: Option[String] = None,
    creationTime: Option[DateTime] = None,
    iso: Option[Int] = None,
    lensModel: Option[String] = None)(implicit session: DBSession = autoSession): Metadata = {
    val generatedKey = withSQL {
      insert.into(Metadata).columns(
        column.imageId,
        column.cameraModel,
        column.creationTime,
        column.iso,
        column.lensModel
      ).values(
          imageId,
          cameraModel,
          creationTime,
          iso,
          lensModel
        )
    }.updateAndReturnGeneratedKey.apply()

    Metadata(
      id = generatedKey.toInt,
      imageId = imageId,
      cameraModel = cameraModel,
      creationTime = creationTime,
      iso = iso,
      lensModel = lensModel)
  }

  def save(entity: Metadata)(implicit session: DBSession = autoSession): Metadata = {
    withSQL {
      update(Metadata).set(
        column.id -> entity.id,
        column.imageId -> entity.imageId,
        column.cameraModel -> entity.cameraModel,
        column.creationTime -> entity.creationTime,
        column.iso -> entity.iso,
        column.lensModel -> entity.lensModel
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def destroy(entity: Metadata)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(Metadata).where.eq(column.id, entity.id) }.update.apply()
  }

}
