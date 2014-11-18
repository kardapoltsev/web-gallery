package com.github.kardapoltsev.webgallery.db

import scalikejdbc._
import org.joda.time.{ DateTimeZone, DateTime }

/**
 * Created by alexey on 6/19/14.
 */
object Session {
  import gen.Session._

  def create(userId: UserId)(implicit session: DBSession = autoSession): Session =
    create(userId, DateTime.now(DateTimeZone.UTC))

  def delete(sessionId: SessionId)(implicit session: DBSession = autoSession): Unit = {
    withSQL { QueryDSL.delete.from(Session).where.eq(column.id, sessionId) }.update.apply()
  }
}
