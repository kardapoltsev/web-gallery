package com.github.kardapoltsev.webgallery.db.gen

import scalikejdbc._
import org.joda.time.{ DateTime }

case class Like(
  id: Int,
  imageId: Int,
  ownerId: Int,
  createTime: DateTime)

object Like extends SQLSyntaxSupport[Like] {

  override val tableName = "likes"

  override val columns = Seq("id", "image_id", "owner_id", "create_time")

  def apply(l: SyntaxProvider[Like])(rs: WrappedResultSet): Like = apply(l.resultName)(rs)
  def apply(l: ResultName[Like])(rs: WrappedResultSet): Like = new Like(
    id = rs.get(l.id),
    imageId = rs.get(l.imageId),
    ownerId = rs.get(l.ownerId),
    createTime = rs.get(l.createTime)
  )

  val l = Like.syntax("l")

  override val autoSession = AutoSession

  def find(id: Int)(implicit session: DBSession = autoSession): Option[Like] = {
    withSQL {
      select.from(Like as l).where.eq(l.id, id)
    }.map(Like(l.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[Like] = {
    withSQL {
      select.from(Like as l).where.append(sqls"${where}")
    }.map(Like(l.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls"count(1)").from(Like as l).where.append(sqls"${where}")
    }.map(_.long(1)).single.apply().get
  }

  def create(
    imageId: Int,
    ownerId: Int,
    createTime: DateTime)(implicit session: DBSession = autoSession): Like = {
    val generatedKey = withSQL {
      insert.into(Like).columns(
        column.imageId,
        column.ownerId,
        column.createTime
      ).values(
          imageId,
          ownerId,
          createTime
        )
    }.updateAndReturnGeneratedKey.apply()

    Like(
      id = generatedKey.toInt,
      imageId = imageId,
      ownerId = ownerId,
      createTime = createTime)
  }

  def save(entity: Like)(implicit session: DBSession = autoSession): Like = {
    withSQL {
      update(Like).set(
        column.id -> entity.id,
        column.imageId -> entity.imageId,
        column.ownerId -> entity.ownerId,
        column.createTime -> entity.createTime
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def destroy(entity: Like)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(Like).where.eq(column.id, entity.id) }.update.apply()
  }

  def countAll(implicit session: DBSession = autoSession): Int = {
    withSQL {
      select(sqls"count(1)").from(Like as l)
    }.map(_.int(1)).single.apply().get
  }

}
