package com.github.kardapoltsev.webgallery.db.gen

import scalikejdbc._

case class Acl(
  id: Int, 
  tagId: Int, 
  userId: Int) {

  def save()(implicit session: DBSession = Acl.autoSession): Acl = Acl.save(this)(session)

  def destroy()(implicit session: DBSession = Acl.autoSession): Unit = Acl.destroy(this)(session)

}
      

object Acl extends SQLSyntaxSupport[Acl] {

  override val tableName = "acl"

  override val columns = Seq("id", "tag_id", "user_id")

  def apply(a: SyntaxProvider[Acl])(rs: WrappedResultSet): Acl = apply(a.resultName)(rs)
  def apply(a: ResultName[Acl])(rs: WrappedResultSet): Acl = new Acl(
    id = rs.get(a.id),
    tagId = rs.get(a.tagId),
    userId = rs.get(a.userId)
  )
      
  val a = Acl.syntax("a")

  override val autoSession = AutoSession

  def find(id: Int)(implicit session: DBSession = autoSession): Option[Acl] = {
    withSQL {
      select.from(Acl as a).where.eq(a.id, id)
    }.map(Acl(a.resultName)).single.apply()
  }
          
  def findAll()(implicit session: DBSession = autoSession): List[Acl] = {
    withSQL(select.from(Acl as a)).map(Acl(a.resultName)).list.apply()
  }
          
  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls"count(1)").from(Acl as a)).map(rs => rs.long(1)).single.apply().get
  }
          
  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[Acl] = {
    withSQL { 
      select.from(Acl as a).where.append(sqls"${where}")
    }.map(Acl(a.resultName)).list.apply()
  }
      
  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL { 
      select(sqls"count(1)").from(Acl as a).where.append(sqls"${where}")
    }.map(_.long(1)).single.apply().get
  }
      
  def create(
    tagId: Int,
    userId: Int)(implicit session: DBSession = autoSession): Acl = {
    val generatedKey = withSQL {
      insert.into(Acl).columns(
        column.tagId,
        column.userId
      ).values(
        tagId,
        userId
      )
    }.updateAndReturnGeneratedKey.apply()

    Acl(
      id = generatedKey.toInt, 
      tagId = tagId,
      userId = userId)
  }

  def save(entity: Acl)(implicit session: DBSession = autoSession): Acl = {
    withSQL {
      update(Acl).set(
        column.id -> entity.id,
        column.tagId -> entity.tagId,
        column.userId -> entity.userId
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }
        
  def destroy(entity: Acl)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(Acl).where.eq(column.id, entity.id) }.update.apply()
  }
        
}
