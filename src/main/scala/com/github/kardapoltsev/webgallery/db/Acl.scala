package com.github.kardapoltsev.webgallery.db

import com.github.kardapoltsev.webgallery.util.Hardcoded
import scalikejdbc._

/**
 * Created by alexey on 6/23/14.
 */
object Acl {
  import gen.Acl._
  import gen.ImageTag.it
  import gen.Image.i

  def delete(tagId: TagId, userId: UserId)(implicit session: DBSession = autoSession): Unit = {
    withSQL {
      QueryDSL.delete.from(Acl).where
        .eq(column.tagId, tagId).and.eq(column.userId, userId)
    }.update().apply()
  }

  def findByTagId(tagId: TagId)(implicit session: DBSession = autoSession): Seq[Acl] = {
    findAllBy(sqls.eq(column.tagId, tagId))
  }

  //TODO: replace with exists
  def existsForTag(tagId: TagId, userId: UserId)(implicit session: DBSession = autoSession): Boolean = {
    countBy(sqls.eq(column.tagId, tagId).and.eq(column.userId, userId)) > 0
  }

  def existsForImage(imageId: TagId, userId: UserId)(implicit session: DBSession = autoSession): Boolean = {
    withSQL {
      select(sqls"count(1) > 0").from(Image as i).leftJoin(ImageTag as it).on(it.imageId, i.id).
        where.eq(i.id, imageId).and.exists(
          select(sqls"1").from(Acl as a).
            where.eq(a.tagId, it.tagId).and.in(a.userId, Seq(userId, Hardcoded.AnonymousUserId))
        ).or.eq(i.ownerId, userId)
    }
  }.map(_.boolean(1)).single().apply().get

}
