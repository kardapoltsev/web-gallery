package com.github.kardapoltsev.webgallery.db.gen


import com.github.kardapoltsev.webgallery.db.SessionId
import com.github.kardapoltsev.webgallery.util.IdGenerator
import scalikejdbc._
import org.joda.time.{DateTime}

case class Session(
  id: String,
  userId: Int, 
  updateTime: DateTime) {

  def save()(implicit session: DBSession = Session.autoSession): Session = Session.save(this)(session)

  def destroy()(implicit session: DBSession = Session.autoSession): Unit = Session.destroy(this)(session)

}
      

object Session extends SQLSyntaxSupport[Session] {

  override val tableName = "sessions"

  override val columns = Seq("id", "user_id", "update_time")

  def apply(s: SyntaxProvider[Session])(rs: WrappedResultSet): Session = apply(s.resultName)(rs)
  def apply(s: ResultName[Session])(rs: WrappedResultSet): Session = new Session(
    id = rs.get(s.id),
    userId = rs.get(s.userId),
    updateTime = rs.get(s.updateTime)
  )
      
  val s = Session.syntax("s")

  override val autoSession = AutoSession

  def find(id: SessionId)(implicit session: DBSession = autoSession): Option[Session] = {
    withSQL {
      select.from(Session as s).where.eq(s.id, id)
    }.map(Session(s.resultName)).single.apply()
  }
          
  def findAll()(implicit session: DBSession = autoSession): List[Session] = {
    withSQL(select.from(Session as s)).map(Session(s.resultName)).list.apply()
  }
          
  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls"count(1)").from(Session as s)).map(rs => rs.long(1)).single.apply().get
  }
          
  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[Session] = {
    withSQL { 
      select.from(Session as s).where.append(sqls"${where}")
    }.map(Session(s.resultName)).list.apply()
  }
      
  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL { 
      select(sqls"count(1)").from(Session as s).where.append(sqls"${where}")
    }.map(_.long(1)).single.apply().get
  }
      
  def create(
    userId: Int,
    updateTime: DateTime)(implicit session: DBSession = autoSession): Session = {
    val id = IdGenerator.nextSessionId
    withSQL {
      insert.into(Session).columns(
        column.id,
        column.userId,
        column.updateTime
      ).values(
        id,
        userId,
        updateTime
      )
    }.update().apply()

    Session(
      id = id,
      userId = userId,
      updateTime = updateTime)
  }

  def save(entity: Session)(implicit session: DBSession = autoSession): Session = {
    withSQL {
      update(Session).set(
        column.id -> entity.id,
        column.userId -> entity.userId,
        column.updateTime -> entity.updateTime
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }
        
  def destroy(entity: Session)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(Session).where.eq(column.id, entity.id) }.update.apply()
  }
        
}
