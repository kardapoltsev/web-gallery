package com.github.kardapoltsev.webgallery.db.gen

import scalikejdbc._

case class Credentials(
    id: Int,
    authId: String,
    authType: String,
    passwordHash: Option[String] = None,
    userId: Int) {

  def save()(implicit session: DBSession = Credentials.autoSession): Credentials = Credentials.save(this)(session)

  def destroy()(implicit session: DBSession = Credentials.autoSession): Unit = Credentials.destroy(this)(session)

}

object Credentials extends SQLSyntaxSupport[Credentials] {

  override val tableName = "credentials"

  override val columns = Seq("id", "auth_id", "auth_type", "password_hash", "user_id")

  def apply(c: SyntaxProvider[Credentials])(rs: WrappedResultSet): Credentials = apply(c.resultName)(rs)
  def apply(c: ResultName[Credentials])(rs: WrappedResultSet): Credentials = new Credentials(
    id = rs.get(c.id),
    authId = rs.get(c.authId),
    authType = rs.get(c.authType),
    passwordHash = rs.get(c.passwordHash),
    userId = rs.get(c.userId)
  )

  val c = Credentials.syntax("c")

  override val autoSession = AutoSession

  def find(id: Int)(implicit session: DBSession = autoSession): Option[Credentials] = {
    withSQL {
      select.from(Credentials as c).where.eq(c.id, id)
    }.map(Credentials(c.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[Credentials] = {
    withSQL {
      select.from(Credentials as c).where.append(sqls"${where}")
    }.map(Credentials(c.resultName)).list.apply()
  }

  def create(
    authId: String,
    authType: String,
    passwordHash: Option[String] = None,
    userId: Int)(implicit session: DBSession = autoSession): Credentials = {
    val generatedKey = withSQL {
      insert.into(Credentials).columns(
        column.authId,
        column.authType,
        column.passwordHash,
        column.userId
      ).values(
          authId,
          authType,
          passwordHash,
          userId
        )
    }.updateAndReturnGeneratedKey.apply()

    Credentials(
      id = generatedKey.toInt,
      authId = authId,
      authType = authType,
      passwordHash = passwordHash,
      userId = userId)
  }

  def save(entity: Credentials)(implicit session: DBSession = autoSession): Credentials = {
    withSQL {
      update(Credentials).set(
        column.id -> entity.id,
        column.authId -> entity.authId,
        column.authType -> entity.authType,
        column.passwordHash -> entity.passwordHash,
        column.userId -> entity.userId
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def destroy(entity: Credentials)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(Credentials).where.eq(column.id, entity.id) }.update.apply()
  }

}
