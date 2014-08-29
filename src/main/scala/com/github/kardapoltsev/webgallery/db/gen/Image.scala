package com.github.kardapoltsev.webgallery.db.gen

import scalikejdbc._

case class Image(
  id: Int, 
  name: String, 
  filename: String, 
  owner: User,
  ownerId: Int) {

  def save()(implicit session: DBSession = Image.autoSession): Image = Image.save(this)(session)

  def destroy()(implicit session: DBSession = Image.autoSession): Unit = Image.destroy(this)(session)

}
      

object Image extends SQLSyntaxSupport[Image] {

  override val tableName = "image"

  override val columns = Seq("id", "name", "filename", "owner_id")

  def apply(i: SyntaxProvider[Image], o: SyntaxProvider[User])(rs: WrappedResultSet): Image =
    apply(i.resultName, o.resultName)(rs)
  def apply(i: ResultName[Image], o: ResultName[User])(rs: WrappedResultSet): Image = new Image(
    id = rs.get(i.id),
    name = rs.get(i.name),
    filename = rs.get(i.filename),
    owner = User(o)(rs),
    ownerId = rs.get(i.ownerId)
  )
      
  val i = Image.syntax("i")
  val u = User.u

  override val autoSession = AutoSession

  def find(id: Int)(implicit session: DBSession = autoSession): Option[Image] = {
    withSQL {
      select.from(Image as i).innerJoin(User as u).on(i.ownerId, u.id).where.eq(i.id, id)
    }.map(Image(i, u)).single.apply()
  }
          
  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[Image] = {
    withSQL { 
      select.from(Image as i).innerJoin(User as u).on(i.ownerId, u.id).where.append(sqls"${where}")
    }.map(Image(i, u)).list.apply()
  }
      
  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL { 
      select(sqls"count(1)").from(Image as i).where.append(sqls"${where}")
    }.map(_.long(1)).single.apply().get
  }
      
  def create(
    name: String,
    filename: String,
    ownerId: Int)(implicit session: DBSession = autoSession): Image = {
    val generatedKey = withSQL {
      insert.into(Image).columns(
        column.name,
        column.filename,
        column.ownerId
      ).values(
        name,
        filename,
        ownerId
      )
    }.updateAndReturnGeneratedKey.apply()

    val owner = User.find(ownerId).get //TODO: is this needed?
    Image(
      id = generatedKey.toInt, 
      name = name,
      filename = filename,
      owner = owner,
      ownerId = ownerId
    )
  }

  def save(entity: Image)(implicit session: DBSession = autoSession): Image = {
    withSQL {
      update(Image).set(
        column.id -> entity.id,
        column.name -> entity.name,
        column.filename -> entity.filename,
        column.ownerId -> entity.owner.id
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }
        
  def destroy(entity: Image)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(Image).where.eq(column.id, entity.id) }.update.apply()
  }
        
}
