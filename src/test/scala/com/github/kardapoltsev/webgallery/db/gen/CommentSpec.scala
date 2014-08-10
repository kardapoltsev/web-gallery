package com.github.kardapoltsev.webgallery.db.gen

import org.scalatest._
import org.joda.time._
import scalikejdbc.scalatest.AutoRollback
import scalikejdbc._
import com.github.kardapoltsev.webgallery.Database

class CommentSpec extends fixture.FlatSpec with Matchers with AutoRollback with FakeDataCreator {
  val c = Comment.syntax("c")

  Database
  behavior of "Comment"

  it should "find by primary keys" in { implicit session =>
    createComment()
    val maybeFound = Comment.find(commentId)
    maybeFound.isDefined should be(true)
  }
  it should "find all records" in { implicit session =>
    createComment()
    val allResults = Comment.findAll()
    allResults.size should be >(0)
  }
  it should "count all records" in { implicit session =>
    createComment()
    val count = Comment.countAll()
    count should be >(0L)
  }
  it should "find by where clauses" in { implicit session =>
    createComment()
    val results = Comment.findAllBy(sqls.eq(c.id, commentId))
    results.size should be >(0)
  }
  it should "count by where clauses" in { implicit session =>
    createComment()
    val count = Comment.countBy(sqls.eq(c.id, commentId))
    count should be >(0L)
  }
  it should "create new record" in { implicit session =>
    createImage()
    val created = Comment.create(imageId = commentId, text = "MyString", createTime = DateTime.now, ownerId = commentId)
    created should not be(null)
  }
  it should "save a record" in { implicit session =>
    createComment()
    val entity = Comment.findAll().head
    // TODO modify something
    val modified = entity.copy(text = "new")
    val updated = Comment.save(modified)
    updated should not equal(entity)
  }
  it should "destroy a record" in { implicit session =>
    createComment()
    val entity = Comment.findAll().head
    Comment.destroy(entity)
    val shouldBeNone = Comment.find(commentId)
    shouldBeNone.isDefined should be(false)
  }

}
        