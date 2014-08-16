package com.github.kardapoltsev.webgallery.db

import org.joda.time.{DateTimeZone, DateTime}
import scalikejdbc._
import spray.json.{JsonFormat, DefaultJsonProtocol}



/**
 * Created by alexey on 6/17/14.
 */
object Comment {
  import gen.Comment._

  def findByImageId(imageId: ImageId, offset: Int, limit: Int): Seq[Comment] = {
    findAllBy(sqls.eq(column.imageId, imageId).orderBy(column.parentCommentId, column.id).offset(offset).limit(limit))
  }
}


case class CommentDto(
    id: Int,
    imageId: Int,
    parentCommentId: Option[Int] = None,
    text: String,
    createTime: DateTime,
    ownerId: Int,
    replies: Seq[CommentDto] = Seq.empty
    )
object CommentDto extends DefaultJsonProtocol {
  implicit val _: JsonFormat[CommentDto] = lazyFormat(jsonFormat7(CommentDto.apply))
}