package com.github.kardapoltsev.webgallery.db.gen

import com.github.kardapoltsev.webgallery.db.{ SessionId }
import com.github.kardapoltsev.webgallery.util.IdGenerator
import scalikejdbc._
import org.joda.time.{ DateTime }

case class Session(
    id: String,
    userId: Int,
    updateTime: DateTime,
    userAgent: Option[String]) {

  def save()(implicit session: DBSession = Session.autoSession): Session = Session.save(this)(session)

  def destroy()(implicit session: DBSession = Session.autoSession): Unit = Session.destroy(this)(session)

}

object Session extends SQLSyntaxSupport[Session] {

  override val tableName = "sessions"

  override val columns = Seq("id", "user_id", "update_time", "user_agent")

  def apply(s: SyntaxProvider[Session])(rs: WrappedResultSet): Session = apply(s.resultName)(rs)
  def apply(s: ResultName[Session])(rs: WrappedResultSet): Session = new Session(
    id = rs.get(s.id),
    userId = rs.get(s.userId),
    updateTime = rs.get(s.updateTime),
    userAgent = rs.get(s.userAgent)
  )

  val s = Session.syntax("s")

  override val autoSession = AutoSession

  def find(id: SessionId)(implicit session: DBSession = autoSession): Option[Session] = {
    withSQL {
      select.from(Session as s).where.eq(s.id, id)
    }.map(Session(s.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[Session] = {
    withSQL {
      select.from(Session as s).where.append(sqls"${where}")
    }.map(Session(s.resultName)).list.apply()
  }

  def create(
    userId: Int,
    updateTime: DateTime, userAgent: Option[String])(implicit session: DBSession = autoSession): Session = {
    val id = IdGenerator.nextSessionId
    withSQL {
      insert.into(Session).columns(
        column.id,
        column.userId,
        column.updateTime,
        column.userAgent
      ).values(
          id,
          userId,
          updateTime,
          userAgent
        )
    }.update().apply()

    Session(
      id = id,
      userId = userId,
      updateTime = updateTime,
      userAgent = userAgent)
  }

  def save(entity: Session)(implicit session: DBSession = autoSession): Session = {
    withSQL {
      update(Session).set(
        column.id -> entity.id,
        column.userId -> entity.userId,
        column.updateTime -> entity.updateTime,
        column.userAgent -> entity.userAgent
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def destroy(entity: Session)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(Session).where.eq(column.id, entity.id) }.update.apply()
  }

}
