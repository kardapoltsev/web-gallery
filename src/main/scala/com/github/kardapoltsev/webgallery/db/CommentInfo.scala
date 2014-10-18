package com.github.kardapoltsev.webgallery.db


import org.joda.time.DateTime
import scalikejdbc._
import spray.json.DefaultJsonProtocol


case class CommentInfo(
    id: Int,
    imageId: Int,
    parentCommentId: Option[Int] = None,
    text: String,
    createTime: DateTime,
    owner: User,
    ownerId: Int)


object CommentInfo extends SQLSyntaxSupport[CommentInfo] with DefaultJsonProtocol {

  override val tableName = "comment"

  override val columns = Seq("id", "image_id", "parent_comment_id", "text", "create_time", "owner_id")

  def apply(c: SyntaxProvider[CommentInfo], o: SyntaxProvider[User])(rs: WrappedResultSet): CommentInfo =
    apply(c.resultName, o.resultName)(rs)
  def apply(c: ResultName[CommentInfo], o: ResultName[User])(rs: WrappedResultSet): CommentInfo = new CommentInfo(
    id = rs.get(c.id),
    imageId = rs.get(c.imageId),
    parentCommentId = rs.get(c.parentCommentId),
    text = rs.get(c.text),
    createTime = rs.get(c.createTime),
    owner = User(o)(rs),
    ownerId = rs.get(c.ownerId)
  )
      
  val c = CommentInfo.syntax("c")
  val u = User.u

  override val autoSession = AutoSession


  def findByImageId(imageId: ImageId, offset: Int, limit: Int)(implicit session: DBSession): Seq[CommentInfo] = {
    findBy(offset, limit)(sqls.eq(column.imageId, imageId).orderBy(c.parentCommentId.asc, c.id.asc))
  }


  private def findBy(offset: Int, limit: Int)(where: SQLSyntax)(implicit session: DBSession): Seq[CommentInfo] = {
    withSQL {
      select
          .from(CommentInfo as c)
          .join(User as u).on(c.ownerId, u.id)
          .where.append(where).offset(offset).limit(limit)
    }.map(CommentInfo(c, u)).list().apply()
  }


  implicit val _ = jsonFormat7(CommentInfo.apply)
}
