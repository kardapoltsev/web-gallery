package com.github.kardapoltsev.webgallery.db.gen

import com.github.kardapoltsev.webgallery.db.{ImageId, UserId}
import org.joda.time.DateTime
import scalikejdbc._

case class Tag(
    id: Int,
    ownerId: UserId,
    name: String,
    updateTime: DateTime,
    coverId: ImageId,
    manualCover: Boolean,
    /**
     * This tags used by system eg for untagged, all user tags
     */
    system: Boolean,
    /**
     * Marker for all automatically assigned tags
     */
    auto: Boolean
    ) {

  def save()(implicit session: DBSession = Tag.autoSession): Tag = Tag.save(this)(session)

  def destroy()(implicit session: DBSession = Tag.autoSession): Unit = Tag.destroy(this)(session)

}
      

object Tag extends SQLSyntaxSupport[Tag] {

  override val tableName = "tags"

  override val columns = Seq("id", "owner_id", "name", "update_time", "cover_id", "manual_cover", "system", "auto")

  def apply(t: SyntaxProvider[Tag])(rs: WrappedResultSet): Tag = apply(t.resultName)(rs)
  def apply(t: ResultName[Tag])(rs: WrappedResultSet): Tag = new Tag(
    id = rs.get(t.id),
    ownerId = rs.get(t.ownerId),
    name = rs.get(t.name),
    updateTime = rs.get(t.updateTime),
    coverId = rs.get(t.coverId),
    manualCover = rs.get(t.manualCover),
    system = rs.get(t.system),
    auto = rs.get(t.auto)
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
      
  def create(ownerId: UserId,
    name: String, updateTime: DateTime, coverId: ImageId, manualCover: Boolean, system: Boolean, auto: Boolean)
      (implicit session: DBSession = autoSession): Tag = {
    val generatedKey = withSQL {
      insert.into(Tag).columns(
        column.ownerId, column.name, column.updateTime, column.coverId, column.manualCover, column.system, column.auto
      ).values(
        ownerId, name, updateTime, coverId, manualCover, system, auto
      )
    }.updateAndReturnGeneratedKey.apply()

    Tag(
      id = generatedKey.toInt,
      ownerId = ownerId,
      name = name,
      updateTime = updateTime,
      coverId = coverId,
      manualCover = manualCover,
      system = system,
      auto = auto
    )
  }

  def save(entity: Tag)(implicit session: DBSession = autoSession): Tag = {
    withSQL {
      update(Tag).set(
        column.id -> entity.id,
        column.name -> entity.name,
        column.updateTime -> entity.updateTime,
        column.coverId -> entity.coverId,
        column.manualCover -> entity.manualCover
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }
        
  def destroy(entity: Tag)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(Tag).where.eq(column.id, entity.id) }.update.apply()
  }
        
}
