package com.github.kardapoltsev.webgallery.db.gen

import scalikejdbc._
import org.joda.time.{ DateTime }

case class Comment(
    id: Int,
    imageId: Int,
    parentCommentId: Option[Int] = None,
    text: String,
    createTime: DateTime,
    ownerId: Int) {

  def save()(implicit session: DBSession = Comment.autoSession): Comment = Comment.save(this)(session)

  def destroy()(implicit session: DBSession = Comment.autoSession): Unit = Comment.destroy(this)(session)

}

object Comment extends SQLSyntaxSupport[Comment] {

  override val tableName = "comment"

  override val columns = Seq("id", "image_id", "parent_comment_id", "text", "create_time", "owner_id")

  def apply(c: SyntaxProvider[Comment])(rs: WrappedResultSet): Comment = apply(c.resultName)(rs)
  def apply(c: ResultName[Comment])(rs: WrappedResultSet): Comment = new Comment(
    id = rs.get(c.id),
    imageId = rs.get(c.imageId),
    parentCommentId = rs.get(c.parentCommentId),
    text = rs.get(c.text),
    createTime = rs.get(c.createTime),
    ownerId = rs.get(c.ownerId)
  )

  val c = Comment.syntax("c")

  override val autoSession = AutoSession

  def find(id: Int)(implicit session: DBSession = autoSession): Option[Comment] = {
    withSQL {
      select.from(Comment as c).where.eq(c.id, id)
    }.map(Comment(c.resultName)).single.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls"count(1)").from(Comment as c)).map(rs => rs.long(1)).single.apply().get
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[Comment] = {
    withSQL {
      select.from(Comment as c).where.append(sqls"${where}")
    }.map(Comment(c.resultName)).list.apply()
  }

  def create(
    imageId: Int,
    parentCommentId: Option[Int] = None,
    text: String,
    createTime: DateTime,
    ownerId: Int)(implicit session: DBSession = autoSession): Comment = {
    val generatedKey = withSQL {
      insert.into(Comment).columns(
        column.imageId,
        column.parentCommentId,
        column.text,
        column.createTime,
        column.ownerId
      ).values(
          imageId,
          parentCommentId,
          text,
          createTime,
          ownerId
        )
    }.updateAndReturnGeneratedKey.apply()

    Comment(
      id = generatedKey.toInt,
      imageId = imageId,
      parentCommentId = parentCommentId,
      text = text,
      createTime = createTime,
      ownerId = ownerId)
  }

  def save(entity: Comment)(implicit session: DBSession = autoSession): Comment = {
    withSQL {
      update(Comment).set(
        column.id -> entity.id,
        column.imageId -> entity.imageId,
        column.parentCommentId -> entity.parentCommentId,
        column.text -> entity.text,
        column.createTime -> entity.createTime,
        column.ownerId -> entity.ownerId
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def destroy(entity: Comment)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(Comment).where.eq(column.id, entity.id) }.update.apply()
  }

}
