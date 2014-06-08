package com.github.kardapoltsev.webgallery.db

import scalikejdbc._
import com.github.kardapoltsev.webgallery.processing.{ScaleType, SpecificSize}

object Alternative {
  import gen.Alternative._

  def find(imageId: Int, size: SpecificSize)(implicit session: DBSession = autoSession): Option[Alternative] = {
    withSQL {
      select.from(Alternative as a)
        .where(sqls.toAndConditionOpt(
          Some(sqls.eq(a.imageId, imageId)),
          Some(sqls.ge(a.width, size.width)),
          Some(sqls.ge(a.height, size.height)),
          Some(sqls.eq(a.scaleType, ScaleType.FitSource.toString))
        ))
        .orderBy(a.width, a.height).asc
        .limit(1)
    }.map(Alternative(a.resultName)).single().apply()
  }


  def create(imageId: Int, filename: String, size: SpecificSize): Alternative =
    Alternative.create(imageId, filename, size.width, size.height, size.scaleType.toString)
}
