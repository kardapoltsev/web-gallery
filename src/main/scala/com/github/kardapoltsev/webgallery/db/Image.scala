package com.github.kardapoltsev.webgallery.db


import scalikejdbc._



object Image {
  import gen.Image._
  import gen.ImageTag.it

  def findByTag(tagId: TagId, offset: Int, limit: Int)(implicit session: DBSession = autoSession): Seq[Image] = {
    withSQL {
      select.from(Image as i)
          .join(ImageTag as it).on(it.imageId, i.id)
          .where.eq(it.tagId, tagId).offset(offset).limit(limit)
    }.map(gen.Image(i)).list().apply()
  }
}
