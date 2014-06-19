package com.github.kardapoltsev.webgallery.db


import org.joda.time.{DateTimeZone, DateTime}
import scalikejdbc.DBSession



/**
 * Created by alexey on 6/17/14.
 */
object User {
  import gen.User._

  def create(name: String)(implicit session: DBSession = autoSession): User =
    create(name, DateTime.now(DateTimeZone.UTC))
}
