package com.github.kardapoltsev.webgallery.db

import scalikejdbc._


object Image {
  import gen.Image._
  import gen.ImageTag.it
  import gen.Tag.t
  import gen.Acl.a
  import gen.User.u


  /**
   * Find images that user can read
   * @param userId requester userId
   */
  def findByTag(tagId: TagId, userId: UserId)(implicit session: DBSession = autoSession): Seq[Image] = {
    withSQL {
      select.from(Image as i)
        .join(ImageTag as it).on(it.imageId, i.id)
        .join(Tag as t).on(it.tagId, t.id)
        .join(User as u).on(i.ownerId, u.id)
        .where.eq(t.id, tagId).and
        .withRoundBracket(_.eq(i.ownerId, userId)
                           .or.exists(select.from(Acl as a).where.eq(a.userId, userId).and.eq(a.tagId, t.id)))
    }.map(Image(i, u)).list().apply()
  }
}
