package com.github.kardapoltsev.webgallery.db

import scalikejdbc._


object Image {
  import gen.Image._
  import gen.ImageTag.it
  import gen.Tag.t

  def findByTag(tagName: String)(implicit session: DBSession = autoSession): Seq[Image] = {
    withSQL {
      select.from(Image as i)
        .join(ImageTag as it).on(it.imageId, i.id)
        .join(Tag as t).on(it.tagId, t.id)
        .where.eq(t.name, tagName)
    }.map(Image(i.resultName)).list().apply()
  }
}
