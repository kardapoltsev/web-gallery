package com.github.kardapoltsev.webgallery.db


import scalikejdbc._



object Image {
  import gen.Image._
  import gen.ImageTag.it
  import gen.Acl.a
  import gen.Like.l
  import com.github.kardapoltsev.webgallery.util.Hardcoded.AnonymousUserId


  def findByTag(tagId: TagId, offset: Int, limit: Int)(implicit session: DBSession = autoSession): Seq[Image] = {
    withSQL {
      select.from(Image as i)
          .join(ImageTag as it).on(it.imageId, i.id)
          .where.eq(it.tagId, tagId).offset(offset).limit(limit)
    }.map(gen.Image(i)).list().apply()
  }


  def findPopular(requesterId: UserId, offset: Int, limit: Int)
      (implicit session: DBSession = ReadOnlyAutoSession): Seq[Image] = {
    withSQL {
      select(
        i.resultAll,
        sqls"(select count(1) from likes l".where.eq(l.imageId, i.id).append(sqls") as likes_count")
      ).from(Image as i).
          where.exists(
            select.from(Acl as a).
                join(ImageTag as it).on(it.tagId, a.tagId).toSQLSyntax.and.eq(it.imageId, i.id).
                where.in(a.userId, Seq(AnonymousUserId, requesterId))).
          orderBy(sqls"likes_count".desc, i.id).offset(offset).limit(limit)
    }.map(gen.Image(i)).list().apply()
  }

}
