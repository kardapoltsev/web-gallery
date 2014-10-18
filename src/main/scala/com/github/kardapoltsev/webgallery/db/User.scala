package com.github.kardapoltsev.webgallery.db


import com.github.kardapoltsev.webgallery.util.Hardcoded
import org.joda.time.{DateTimeZone, DateTime}
import scalikejdbc._



/**
 * Created by alexey on 6/17/14.
 */
object User {
  import gen.User._

  def create(name: String)(implicit session: DBSession = autoSession): User =
    create(name, Hardcoded.DefaultAvatarId, DateTime.now(DateTimeZone.UTC))

  def search(query: String, requesterId: UserId, offset: Int, limit: Int)
      (implicit session: DBSession): Seq[User] = {
    findAllBy(sqls"search_info @@ to_tsquery(${query + ":*"})".
        and.ne(column.id, requesterId).
        offset(offset).limit(limit))
  }

}
