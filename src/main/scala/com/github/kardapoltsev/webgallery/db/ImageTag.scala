package com.github.kardapoltsev.webgallery.db

import scalikejdbc._


object ImageTag {
  import gen.ImageTag._


  def delete(imageId: ImageId, tagId: TagId)(implicit session: DBSession = autoSession): Unit = {
    withSQL {
      QueryDSL.delete.from(ImageTag).where.eq(column.imageId, imageId).and.eq(column.tagId, tagId)
    }.update.apply()
  }

}
