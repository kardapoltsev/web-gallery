package com.github.kardapoltsev.webgallery.db.gen

import scalikejdbc._

case class Settings(
  id: Int, 
  version: Int) {

  def save()(implicit session: DBSession = Settings.autoSession): Settings = Settings.save(this)(session)

  def destroy()(implicit session: DBSession = Settings.autoSession): Unit = Settings.destroy(this)(session)

}
      

object Settings extends SQLSyntaxSupport[Settings] {

  override val tableName = "settings"

  override val columns = Seq("id", "version")

  def apply(s: SyntaxProvider[Settings])(rs: WrappedResultSet): Settings = apply(s.resultName)(rs)
  def apply(s: ResultName[Settings])(rs: WrappedResultSet): Settings = new Settings(
    id = rs.get(s.id),
    version = rs.get(s.version)
  )
      
  val s = Settings.syntax("s")

  override val autoSession = AutoSession

  def find(id: Int)(implicit session: DBSession = autoSession): Option[Settings] = {
    withSQL {
      select.from(Settings as s).where.eq(s.id, id)
    }.map(Settings(s.resultName)).single.apply()
  }
          
  def findAll()(implicit session: DBSession = autoSession): List[Settings] = {
    withSQL(select.from(Settings as s)).map(Settings(s.resultName)).list.apply()
  }
          
  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls"count(1)").from(Settings as s)).map(rs => rs.long(1)).single.apply().get
  }
          
  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[Settings] = {
    withSQL { 
      select.from(Settings as s).where.append(sqls"${where}")
    }.map(Settings(s.resultName)).list.apply()
  }
      
  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL { 
      select(sqls"count(1)").from(Settings as s).where.append(sqls"${where}")
    }.map(_.long(1)).single.apply().get
  }
      
  def create(
    version: Int)(implicit session: DBSession = autoSession): Settings = {
    val generatedKey = withSQL {
      insert.into(Settings).columns(
        column.version
      ).values(
        version
      )
    }.updateAndReturnGeneratedKey.apply()

    Settings(
      id = generatedKey.toInt, 
      version = version)
  }

  def save(entity: Settings)(implicit session: DBSession = autoSession): Settings = {
    withSQL {
      update(Settings).set(
        column.id -> entity.id,
        column.version -> entity.version
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }
        
  def destroy(entity: Settings)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(Settings).where.eq(column.id, entity.id) }.update.apply()
  }
        
}
