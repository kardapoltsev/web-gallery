package com.github.kardapoltsev.webgallery.db


import org.joda.time.{DateTimeZone, DateTime}
import scalikejdbc._



/**
 * Created by alexey on 8/19/14.
 */
object Like {
  import gen.Like._

  def create(imageId: ImageId, ownerId: UserId)(implicit session: DBSession = autoSession): gen.Like =
    create(imageId, ownerId, DateTime.now(DateTimeZone.UTC))


  def delete(imageId: ImageId, ownerId: UserId)(implicit session: DBSession = autoSession): Unit = {
    withSQL { scalikejdbc.delete.from(Like).
        where.eq(column.imageId, imageId).and.eq(column.ownerId, ownerId) }.update.apply()
  }


  def findByImage(imageId: ImageId, offset: Int, limit: Int)(implicit session: DBSession = autoSession): Seq[gen.Like] =
    findAllBy(sqls.eq(column.imageId, imageId).offset(offset).limit(limit))


  def countByImage(imageId: ImageId)(implicit session: DBSession = autoSession): Long =
    countBy(sqls.eq(column.imageId, imageId))

  def isLiked(imageId: ImageId, userId: UserId)(implicit session: DBSession = autoSession): Boolean =
    countBy(sqls.eq(column.imageId, imageId).and.eq(column.ownerId, userId)) > 0
}
