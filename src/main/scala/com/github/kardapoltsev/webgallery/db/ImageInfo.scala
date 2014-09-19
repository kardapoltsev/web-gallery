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
  isLiked: Boolean = false
  ) {
}
      

object ImageInfo extends SQLSyntaxSupport[Image] with DefaultJsonProtocol {
  import com.github.kardapoltsev.webgallery.util.Hardcoded.AnonymousUserId

  override val tableName = "images"

  override val columns = Seq("id", "name", "filename", "owner_id")

  def apply(i: SyntaxProvider[Image], o: SyntaxProvider[User], likes: Long)(rs: WrappedResultSet): ImageInfo =
    apply(i.resultName, o.resultName, likes)(rs)
  def apply(i: ResultName[Image], o: ResultName[User], likes: Long)(rs: WrappedResultSet): ImageInfo = new ImageInfo(
    id = rs.get(i.id),
    name = rs.get(i.name),
    filename = rs.get(i.filename),
    owner = User(o)(rs),
    ownerId = rs.get(i.ownerId),
    likesCount = likes
  )
      
  val i = Image.syntax("i")
  val u = User.u
  val it = gen.ImageTag.it
  val t = gen.Tag.t
  val a = gen.Acl.a
  val l = gen.Like.l

  override val autoSession = AutoSession


  /**
   * Find images that user can read
   * @param requesterId requester userId
   */
  def findByTag(tagId: TagId, requesterId: UserId, offset: Int, limit: Int)
               (implicit session: DBSession = autoSession): Seq[ImageInfo] = {
    findBy(offset, limit) {
      sqls.eq(t.id, tagId).and.
        exists(select.from(Acl as a).
          where.withRoundBracket(_.eq(a.userId, AnonymousUserId).or.eq(a.userId, requesterId)).
            and.toSQLSyntax.eq(a.tagId, t.id)).
          orderBy(i.id)
    }
  }


  def findPopular(requesterId: UserId, offset: Int, limit: Int)
               (implicit session: DBSession = autoSession): Seq[ImageInfo] = {
      findBy(offset, limit){
        sqls.exists(select.from(Acl as a).
          where.withRoundBracket(_.eq(a.userId, AnonymousUserId).or.eq(a.userId, requesterId)).and.toSQLSyntax.eq(a.tagId, t.id)).
          orderBy(sqls"likes_count desc, i.id")
      }
  }


  private def findBy(offset: Int, limit: Int)(where: SQLSyntax)(implicit session: DBSession): Seq[ImageInfo] = {
    withSQL {
      select(i.resultAll, u.resultAll, t.resultAll,
        sqls"(select count(1) from likes l where l.image_id = i.id) as likes_count").from(Image as i)
        .join(ImageTag as it).on(it.imageId, i.id)
        .join(Tag as t).on(it.tagId, t.id)
        .join(User as u).on(i.ownerId, u.id)
        .where.append(where).offset(offset).limit(limit)
    }.one(rs => ImageInfo(i, u, rs.long("likes_count"))(rs)).
      toMany(rs => Tag.opt(t)(rs)).map{ (image, tags) => image.copy(tags = tags)} .list().apply()
  }


  implicit val _ = jsonFormat8(ImageInfo.apply)
}
