package com.github.kardapoltsev.webgallery.db.gen

import scalikejdbc._

case class Credentials(
  id: Int, 
  authId: String, 
  authType: String, 
  userId: Int) {

  def save()(implicit session: DBSession = Credentials.autoSession): Credentials = Credentials.save(this)(session)

  def destroy()(implicit session: DBSession = Credentials.autoSession): Unit = Credentials.destroy(this)(session)

}
      

object Credentials extends SQLSyntaxSupport[Credentials] {

  override val tableName = "credentials"

  override val columns = Seq("id", "auth_id", "auth_type", "user_id")

  def apply(c: SyntaxProvider[Credentials])(rs: WrappedResultSet): Credentials = apply(c.resultName)(rs)
  def apply(c: ResultName[Credentials])(rs: WrappedResultSet): Credentials = new Credentials(
    id = rs.get(c.id),
    authId = rs.get(c.authId),
    authType = rs.get(c.authType),
    userId = rs.get(c.userId)
  )
      
  val c = Credentials.syntax("c")

  override val autoSession = AutoSession

  def find(id: Int)(implicit session: DBSession = autoSession): Option[Credentials] = {
    withSQL {
      select.from(Credentials as c).where.eq(c.id, id)
    }.map(Credentials(c.resultName)).single.apply()
  }
          
  def findAll()(implicit session: DBSession = autoSession): List[Credentials] = {
    withSQL(select.from(Credentials as c)).map(Credentials(c.resultName)).list.apply()
  }
          
  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls"count(1)").from(Credentials as c)).map(rs => rs.long(1)).single.apply().get
  }
          
  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[Credentials] = {
    withSQL { 
      select.from(Credentials as c).where.append(sqls"${where}")
    }.map(Credentials(c.resultName)).list.apply()
  }
      
  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL { 
      select(sqls"count(1)").from(Credentials as c).where.append(sqls"${where}")
    }.map(_.long(1)).single.apply().get
  }
      
  def create(
    authId: String,
    authType: String,
    userId: Int)(implicit session: DBSession = autoSession): Credentials = {
    val generatedKey = withSQL {
      insert.into(Credentials).columns(
        column.authId,
        column.authType,
        column.userId
      ).values(
        authId,
        authType,
        userId
      )
    }.updateAndReturnGeneratedKey.apply()

    Credentials(
      id = generatedKey.toInt, 
      authId = authId,
      authType = authType,
      userId = userId)
  }

  def save(entity: Credentials)(implicit session: DBSession = autoSession): Credentials = {
    withSQL {
      update(Credentials).set(
        column.id -> entity.id,
        column.authId -> entity.authId,
        column.authType -> entity.authType,
        column.userId -> entity.userId
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }
        
  def destroy(entity: Credentials)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(Credentials).where.eq(column.id, entity.id) }.update.apply()
  }
        
}
