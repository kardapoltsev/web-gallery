package com.github.kardapoltsev.webgallery.db


import scalikejdbc.DBSession
import org.joda.time.{DateTimeZone, DateTime}



/**
 * Created by alexey on 6/19/14.
 */
object Session {
  import gen.Session._

  def create(userId: UserId)(implicit session: DBSession = autoSession): Session =
    create(userId, DateTime.now(DateTimeZone.UTC))
}
