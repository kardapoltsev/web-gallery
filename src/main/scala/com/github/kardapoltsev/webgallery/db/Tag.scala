package com.github.kardapoltsev.webgallery.db

import com.github.kardapoltsev.webgallery.util.Hardcoded
import org.joda.time.{ DateTimeZone, DateTime }
import scalikejdbc._

object Tag {
  import gen.Tag._
  import gen.Image.i
  import gen.ImageTag.it

  def opt(t: SyntaxProvider[Tag])(rs: WrappedResultSet): Option[gen.Tag] =
    rs.longOpt(t.resultName.id).map(_ => gen.Tag(t)(rs))

  def create(ownerId: UserId, name: String, system: Boolean, auto: Boolean)(implicit session: DBSession = autoSession): Tag =
    create(ownerId, name, DateTime.now(), Hardcoded.DefaultCoverId, false, system, auto)

  def findByImageId(imageId: Int)(implicit session: DBSession = autoSession): Seq[Tag] = {
    withSQL {
      select.from(Tag as t)
        .join(ImageTag as it).on(it.tagId, t.id)
        .join(Image as i).on(i.id, it.imageId)
        .where.eq(i.id, imageId)
    }.map(Tag(t.resultName)).list().apply()
  }

  def find(ownerId: UserId, name: String)(implicit session: DBSession = autoSession): Option[Tag] = {
    withSQL {
      select.from(Tag as t).where.eq(t.name, name).and(Some(sqls.eq(t.ownerId, ownerId)))
    }.map(Tag(t.resultName)).single().apply()
  }

  def getRecentTags(ownerId: UserId, offset: Int, limit: Int)(implicit session: DBSession = autoSession): Seq[Tag] = {
    withSQL {
      select.from(Tag as t).where.eq(t.ownerId, ownerId).orderBy(column.updateTime).desc.offset(offset).limit(limit)
    }.map(Tag(t.resultName)).list().apply()
  }

  def findByUserId(ownerId: UserId)(implicit session: DBSession = autoSession): Seq[Tag] = {
    withSQL {
      select.from(Tag as t).where.eq(t.ownerId, ownerId)
    }.map(Tag(t.resultName)).list().apply()
  }

  def search(query: String, offset: Int, limit: Int)(implicit session: DBSession = autoSession): Seq[Tag] = {
    findAllBy(sqls.like(t.name, query + "%").offset(offset).limit(limit))
  }

  def setName(tagId: TagId, name: String)(implicit db: DBSession = autoSession): Unit = {
    withSQL {
      update(gen.Tag).set(
        column.name -> name
      ).where.eq(column.id, tagId)
    }.update.apply()
  }

  def setCoverId(tagId: TagId, coverId: ImageId, manual: Boolean)(implicit db: DBSession = autoSession): Unit = {
    withSQL {
      update(gen.Tag).set(
        column.coverId -> coverId,
        column.manualCover -> manual
      ).where.eq(column.id, tagId).and.
        withRoundBracket(_.eq(column.manualCover, false).or.eq(column.manualCover, manual))
    }.update.apply()
  }

}
