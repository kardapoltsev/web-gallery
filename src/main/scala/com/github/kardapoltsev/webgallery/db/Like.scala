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

}
