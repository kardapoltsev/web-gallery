package com.github.kardapoltsev.webgallery.db.gen

import scalikejdbc._

case class Image(
  id: Int, 
  name: String, 
  filename: String) {

  def save()(implicit session: DBSession = Image.autoSession): Image = Image.save(this)(session)

  def destroy()(implicit session: DBSession = Image.autoSession): Unit = Image.destroy(this)(session)

}
      

object Image extends SQLSyntaxSupport[Image] {

  override val tableName = "image"

  override val columns = Seq("id", "name", "filename")

  def apply(i: SyntaxProvider[Image])(rs: WrappedResultSet): Image = apply(i.resultName)(rs)
  def apply(i: ResultName[Image])(rs: WrappedResultSet): Image = new Image(
    id = rs.get(i.id),
    name = rs.get(i.name),
    filename = rs.get(i.filename)
  )
      
  val i = Image.syntax("i")

  override val autoSession = AutoSession

  def find(id: Int)(implicit session: DBSession = autoSession): Option[Image] = {
    withSQL {
      select.from(Image as i).where.eq(i.id, id)
    }.map(Image(i.resultName)).single.apply()
  }
          
  def findAll()(implicit session: DBSession = autoSession): List[Image] = {
    withSQL(select.from(Image as i)).map(Image(i.resultName)).list.apply()
  }
          
  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls"count(1)").from(Image as i)).map(rs => rs.long(1)).single.apply().get
  }
          
  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[Image] = {
    withSQL { 
      select.from(Image as i).where.append(sqls"${where}")
    }.map(Image(i.resultName)).list.apply()
  }
      
  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL { 
      select(sqls"count(1)").from(Image as i).where.append(sqls"${where}")
    }.map(_.long(1)).single.apply().get
  }
      
  def create(
    name: String,
    filename: String)(implicit session: DBSession = autoSession): Image = {
    val generatedKey = withSQL {
      insert.into(Image).columns(
        column.name,
        column.filename
      ).values(
        name,
        filename
      )
    }.updateAndReturnGeneratedKey.apply()

    Image(
      id = generatedKey.toInt, 
      name = name,
      filename = filename)
  }

  def save(entity: Image)(implicit session: DBSession = autoSession): Image = {
    withSQL {
      update(Image).set(
        column.id -> entity.id,
        column.name -> entity.name,
        column.filename -> entity.filename
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }
        
  def destroy(entity: Image)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(Image).where.eq(column.id, entity.id) }.update.apply()
  }
        
}
