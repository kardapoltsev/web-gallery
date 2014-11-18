package com.github.kardapoltsev.webgallery.db

import scalikejdbc._

/**
 * Created by alexey on 9/3/14.
 */
object Settings {
  import gen.Settings._
  def setVersion(version: Int)(implicit session: DBSession) =
    withSQL {
      update(Settings).set(
        column.version -> version
      )
    }.update().apply()
}
