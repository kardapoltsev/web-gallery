package com.github.kardapoltsev.webgallery.db

import scalikejdbc._

/**
 * Created by alexey on 6/23/14.
 */
object Acl {
  import gen.Acl._

  def delete(tagId: TagId, userId: UserId)(implicit session: DBSession = autoSession): Unit = {
    withSQL { QueryDSL.delete.from(Acl).where
        .eq(column.tagId, tagId).and.eq(column.userId, userId)}.update().apply()
  }


  def findByTagId(tagId: TagId)(implicit session: DBSession = autoSession): Seq[Acl] = {
    findAllBy(sqls.eq(column.tagId, tagId))
  }


  def exists(tagId: TagId, userId: UserId)(implicit session: DBSession = autoSession): Boolean = {
    countBy(sqls.eq(column.tagId, tagId).and.eq(column.userId, userId)) > 0
  }

}
