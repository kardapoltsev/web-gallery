package com.github.kardapoltsev.webgallery.db

import scalikejdbc._

object Tag {
  import gen.Tag._
  import gen.Image.i
  import gen.ImageTag.it


  def findByImageId(imageId: Int)(implicit session: DBSession = autoSession): Seq[Tag] = {
    withSQL {
      select.from(Tag as t)
          .join(ImageTag as it).on(it.tagId, t.id)
          .join(Image as i).on(i.id, it.imageId)
          .where.eq(i.id, imageId)
    }.map(Tag(t.resultName)).list().apply()
  }


  def find(name: String)(implicit session: DBSession = autoSession): Option[Tag] = {
    withSQL {
      select.from(Tag as t).where.eq(t.name, name)
    }.map(Tag(t.resultName)).single().apply()
  }


  def search(query: String)(implicit session: DBSession = autoSession): Seq[Tag] = {
    withSQL {
      select.from(Tag as t).where.like(t.name, query + "%")
    }.map(Tag(t.resultName)).list().apply()
  }

}
