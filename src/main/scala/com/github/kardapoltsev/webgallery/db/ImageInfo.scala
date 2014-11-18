package com.github.kardapoltsev.webgallery.db

import scalikejdbc._
import spray.json.DefaultJsonProtocol

case class ImageInfo(
    id: Int,
    name: String,
    filename: String,
    owner: User,
    ownerId: Int,
    tags: Seq[Tag] = Seq.empty,
    likesCount: Long = 0L,
    isLiked: Boolean = false) {
}

object ImageInfo extends SQLSyntaxSupport[ImageInfo] with DefaultJsonProtocol {
  import com.github.kardapoltsev.webgallery.util.Hardcoded.AnonymousUserId

  override val tableName = "images"

  override val columns = Seq("id", "name", "filename", "owner_id", "likes_count")

  def apply(i: SyntaxProvider[ImageInfo], o: SyntaxProvider[User])(rs: WrappedResultSet): ImageInfo =
    apply(i.resultName, o.resultName)(rs)
  def apply(i: ResultName[ImageInfo], o: ResultName[User])(rs: WrappedResultSet): ImageInfo = new ImageInfo(
    id = rs.get(i.id),
    name = rs.get(i.name),
    filename = rs.get(i.filename),
    owner = User(o)(rs),
    ownerId = rs.get(i.ownerId),
    likesCount = rs.get(i.likesCount)
  )

  val i = ImageInfo.syntax("i")
  val u = User.u
  val it = gen.ImageTag.it
  val t = gen.Tag.t
  val a = gen.Acl.a
  val l = gen.Like.l

  override val autoSession = AutoSession

  def findPopular(requesterId: UserId, offset: Int, limit: Int)(implicit session: DBSession = autoSession): Seq[ImageInfo] = {
    findBy(offset, limit) {
      sqls.exists(select.from(Acl as a).
        join(ImageTag as it).on(it.tagId, a.tagId).
        where.eq(a.userId, AnonymousUserId).or.eq(a.userId, requesterId).toSQLSyntax
      ).orderBy(sqls"likes_count desc, i.id")
    }
  }

  private def findBy(offset: Int, limit: Int)(whereClause: SQLSyntax)(implicit session: DBSession): Seq[ImageInfo] = {
    withSQL {
      select(i.resultAll, u.resultAll, t.resultAll)
        .append(
          sqls"from (select i.*, (select count(1) from likes l " append
            sqls"where l.image_id = i.id) as likes_count " append
            sqls"from images i " append
            sqls.where.append(whereClause) append
            sqls.offset(offset).limit(limit).append(sqls") i ")
        )
        .join(ImageTag as it).on(it.imageId, i.id)
        .join(Tag as t).on(it.tagId, t.id)
        .join(User as u).on(i.ownerId, u.id)
    }.one(rs => ImageInfo(i, u)(rs)).
      toMany(rs => Tag.opt(t)(rs)).map { (image, tags) => image.copy(tags = tags) }.list().apply()
  }

  implicit val _ = jsonFormat8(ImageInfo.apply)
}
