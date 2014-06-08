package com.github.kardapoltsev.webgallery.db

import scalikejdbc._

object Tag {
  import gen.Tag._

  def find(name: String)(implicit session: DBSession = autoSession): Option[Tag] = {
    withSQL {
      select.from(Tag as t).where.eq(t.name, name)
    }.map(Tag(t.resultName)).single().apply()
  }

  def search(name: String)(implicit session: DBSession = autoSession): Seq[Tag] = {
    withSQL {
      select.from(Tag as t).where.like(t.name, name + "%")
    }.map(Tag(t.resultName)).list().apply()
  }
}
