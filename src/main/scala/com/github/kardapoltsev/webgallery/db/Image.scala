package com.github.kardapoltsev.webgallery.db

import scalikejdbc._


object Image {
  import gen.Image._
  import gen.ImageTag.it
  import gen.Tag.t
  import gen.Acl.a
  import gen.User.u
  import com.github.kardapoltsev.webgallery.util.Hardcoded.AnonymousUserId


  /**
   * Find images that user can read
   * @param requesterId requester userId
   */
  def findByTag(tagId: TagId, requesterId: UserId)(implicit session: DBSession = autoSession): Seq[Image] = {
    withSQL {
      select.from(Image as i)
        .join(ImageTag as it).on(it.imageId, i.id)
        .join(Tag as t).on(it.tagId, t.id)
        .join(User as u).on(i.ownerId, u.id)
        .where.eq(t.id, tagId).and
        .withRoundBracket(_.eq(i.ownerId, requesterId).
          or.exists(select.from(Acl as a).
          where.withRoundBracket(_.eq(a.userId, AnonymousUserId).or.eq(a.userId, requesterId)).and.eq(a.tagId, t.id)))
    }.map(Image(i, u)).list().apply()
  }
}
