package com.github.kardapoltsev.webgallery.db

import scalikejdbc._
import com.github.kardapoltsev.webgallery.processing.{ OptionalSize, ScaleType, SpecificSize }

object Alternative {
  import gen.Alternative._

  def find(imageId: Int, size: OptionalSize)(implicit session: DBSession = autoSession): Option[Alternative] = {
    withSQL {
      select.from(Alternative as a)
        .where(sqls.toAndConditionOpt(
          Some(sqls.eq(a.imageId, imageId)),
          size.optWidth.map(w => sqls.ge(a.width, w)),
          size.optHeight.map(h => sqls.ge(a.height, h)),
          Some(sqls.eq(a.scaleType, size.scaleType.toString))
        ))
        .orderBy(a.width.desc, a.height.desc)
        .limit(1)
    }.map(Alternative(a.resultName)).single().apply()
  }

  def findByImageId(imageId: Int)(implicit session: DBSession = autoSession): Seq[Alternative] = {
    withSQL {
      select.from(Alternative as a).where.eq(a.imageId, imageId)
    }.map(Alternative(a.resultName)).list().apply()
  }

  def create(imageId: Int, filename: String, size: OptionalSize)(implicit s: DBSession): Alternative =
    Alternative.create(imageId, filename, size.optWidth, size.optHeight, size.scaleType.toString)

}
