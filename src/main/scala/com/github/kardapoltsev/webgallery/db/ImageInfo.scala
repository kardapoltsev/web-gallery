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

  def apply(i: SyntaxProvider[Image], o: SyntaxProvider[User])(rs: WrappedResultSet): ImageInfo =
    apply(i.resultName, o.resultName)(rs)
  def apply(i: ResultName[Image], o: ResultName[User])(rs: WrappedResultSet): ImageInfo = new ImageInfo(
    id = rs.get(i.id),
    name = rs.get(i.name),
    filename = rs.get(i.filename),
    owner = User(o)(rs),
    ownerId = rs.get(i.ownerId)
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
  def findByTag(tagId: TagId, requesterId: UserId)(implicit session: DBSession = autoSession): Seq[ImageInfo] = {
    withSQL {
      select.from(Image as i)
          .join(ImageTag as it).on(it.imageId, i.id)
          .join(Tag as t).on(it.tagId, t.id)
          .join(User as u).on(i.ownerId, u.id)
          .where.eq(t.id, tagId).and
          .withRoundBracket(_.eq(i.ownerId, requesterId).
          or.exists(select.from(Acl as a).
          where.withRoundBracket(_.eq(a.userId, AnonymousUserId).or.eq(a.userId, requesterId)).and.eq(a.tagId, t.id)))
    }.one(ImageInfo(i, u)).toMany(rs => Tag.opt(t)(rs)).map{ (image, tags) => image.copy(tags = tags)} .list().apply()
  }


  implicit val _ = jsonFormat8(ImageInfo.apply)
}
