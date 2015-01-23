package com.github.kardapoltsev.webgallery.db.gen

import scalikejdbc._
import org.joda.time.{ DateTime }

case class User(
    id: Int,
    name: String,
    avatarId: Int,
    registrationTime: DateTime) {

  def save()(implicit session: DBSession = User.autoSession): User = User.save(this)(session)

  def destroy()(implicit session: DBSession = User.autoSession): Unit = User.destroy(this)(session)

}

object User extends SQLSyntaxSupport[User] {

  override val tableName = "users"

  override val columns = Seq("id", "name", "avatar_id", "registration_time")

  def apply(u: SyntaxProvider[User])(rs: WrappedResultSet): User = apply(u.resultName)(rs)
  def apply(u: ResultName[User])(rs: WrappedResultSet): User = new User(
    id = rs.get(u.id),
    name = rs.get(u.name),
    avatarId = rs.get(u.avatarId),
    registrationTime = rs.get(u.registrationTime)
  )

  val u = User.syntax("u")

  override val autoSession = AutoSession

  def find(id: Int)(implicit session: DBSession = autoSession): Option[User] = {
    withSQL {
      select.from(User as u).where.eq(u.id, id)
    }.map(User(u.resultName)).single.apply()
  }

  def countAll(implicit session: DBSession = autoSession): Int = {
    withSQL(select(sqls"count(1)").from(User as u)).map(rs => rs.int(1)).single.apply().get
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[User] = {
    withSQL {
      select.from(User as u).where.append(sqls"${where}")
    }.map(User(u.resultName)).list.apply()
  }

  def create(
    name: String,
    avatarId: Int,
    registrationTime: DateTime)(implicit session: DBSession = autoSession): User = {
    val generatedKey = withSQL {
      insert.into(User).columns(
        column.name,
        column.avatarId,
        column.registrationTime,
        sqls"search_info"
      ).values(
          name,
          avatarId,
          registrationTime,
          sqls"to_tsvector($name)"
        )
    }.updateAndReturnGeneratedKey.apply()

    User(
      id = generatedKey.toInt,
      name = name,
      avatarId = avatarId,
      registrationTime = registrationTime)
  }

  def save(entity: User)(implicit session: DBSession = autoSession): User = {
    withSQL {
      update(User).set(
        column.id -> entity.id,
        column.name -> entity.name,
        column.avatarId -> entity.avatarId,
        column.registrationTime -> entity.registrationTime
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def destroy(entity: User)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(User).where.eq(column.id, entity.id) }.update.apply()
  }

}
