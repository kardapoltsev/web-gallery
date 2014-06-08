package com.github.kardapoltsev.webgallery.db.gen

import scalikejdbc._

case class Tag(
  id: Int, 
  name: String) {

  def save()(implicit session: DBSession = Tag.autoSession): Tag = Tag.save(this)(session)

  def destroy()(implicit session: DBSession = Tag.autoSession): Unit = Tag.destroy(this)(session)

}
      

object Tag extends SQLSyntaxSupport[Tag] {

  override val tableName = "tag"

  override val columns = Seq("id", "name")

  def apply(t: SyntaxProvider[Tag])(rs: WrappedResultSet): Tag = apply(t.resultName)(rs)
  def apply(t: ResultName[Tag])(rs: WrappedResultSet): Tag = new Tag(
    id = rs.get(t.id),
    name = rs.get(t.name)
  )
      
  val t = Tag.syntax("t")

  override val autoSession = AutoSession

  def find(id: Int)(implicit session: DBSession = autoSession): Option[Tag] = {
    withSQL {
      select.from(Tag as t).where.eq(t.id, id)
    }.map(Tag(t.resultName)).single.apply()
  }
          
  def findAll()(implicit session: DBSession = autoSession): List[Tag] = {
    withSQL(select.from(Tag as t)).map(Tag(t.resultName)).list.apply()
  }
          
  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls"count(1)").from(Tag as t)).map(rs => rs.long(1)).single.apply().get
  }
          
  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[Tag] = {
    withSQL { 
      select.from(Tag as t).where.append(sqls"${where}")
    }.map(Tag(t.resultName)).list.apply()
  }
      
  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL { 
      select(sqls"count(1)").from(Tag as t).where.append(sqls"${where}")
    }.map(_.long(1)).single.apply().get
  }
      
  def create(
    name: String)(implicit session: DBSession = autoSession): Tag = {
    val generatedKey = withSQL {
      insert.into(Tag).columns(
        column.name
      ).values(
        name
      )
    }.updateAndReturnGeneratedKey.apply()

    Tag(
      id = generatedKey.toInt, 
      name = name)
  }

  def save(entity: Tag)(implicit session: DBSession = autoSession): Tag = {
    withSQL {
      update(Tag).set(
        column.id -> entity.id,
        column.name -> entity.name
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }
        
  def destroy(entity: Tag)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(Tag).where.eq(column.id, entity.id) }.update.apply()
  }
        
}
