package com.github.kardapoltsev.webgallery.db.gen

import scalikejdbc._
import org.joda.time.{DateTime}

case class Metadata(
  id: Int, 
  imageId: Int, 
  cameraModel: Option[String] = None, 
  creationTime: Option[DateTime] = None) {

  def save()(implicit session: DBSession = Metadata.autoSession): Metadata = Metadata.save(this)(session)

  def destroy()(implicit session: DBSession = Metadata.autoSession): Unit = Metadata.destroy(this)(session)

}
      

object Metadata extends SQLSyntaxSupport[Metadata] {

  override val tableName = "metadata"

  override val columns = Seq("id", "image_id", "camera_model", "creation_time")

  def apply(m: SyntaxProvider[Metadata])(rs: WrappedResultSet): Metadata = apply(m.resultName)(rs)
  def apply(m: ResultName[Metadata])(rs: WrappedResultSet): Metadata = new Metadata(
    id = rs.get(m.id),
    imageId = rs.get(m.imageId),
    cameraModel = rs.get(m.cameraModel),
    creationTime = rs.get(m.creationTime)
  )
      
  val m = Metadata.syntax("m")

  override val autoSession = AutoSession

  def find(id: Int)(implicit session: DBSession = autoSession): Option[Metadata] = {
    withSQL {
      select.from(Metadata as m).where.eq(m.id, id)
    }.map(Metadata(m.resultName)).single.apply()
  }
          
  def findAll()(implicit session: DBSession = autoSession): List[Metadata] = {
    withSQL(select.from(Metadata as m)).map(Metadata(m.resultName)).list.apply()
  }
          
  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls"count(1)").from(Metadata as m)).map(rs => rs.long(1)).single.apply().get
  }
          
  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[Metadata] = {
    withSQL { 
      select.from(Metadata as m).where.append(sqls"${where}")
    }.map(Metadata(m.resultName)).list.apply()
  }
      
  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL { 
      select(sqls"count(1)").from(Metadata as m).where.append(sqls"${where}")
    }.map(_.long(1)).single.apply().get
  }
      
  def create(
    imageId: Int,
    cameraModel: Option[String] = None,
    creationTime: Option[DateTime] = None)(implicit session: DBSession = autoSession): Metadata = {
    val generatedKey = withSQL {
      insert.into(Metadata).columns(
        column.imageId,
        column.cameraModel,
        column.creationTime
      ).values(
        imageId,
        cameraModel,
        creationTime
      )
    }.updateAndReturnGeneratedKey.apply()

    Metadata(
      id = generatedKey.toInt, 
      imageId = imageId,
      cameraModel = cameraModel,
      creationTime = creationTime)
  }

  def save(entity: Metadata)(implicit session: DBSession = autoSession): Metadata = {
    withSQL {
      update(Metadata).set(
        column.id -> entity.id,
        column.imageId -> entity.imageId,
        column.cameraModel -> entity.cameraModel,
        column.creationTime -> entity.creationTime
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }
        
  def destroy(entity: Metadata)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(Metadata).where.eq(column.id, entity.id) }.update.apply()
  }
        
}
