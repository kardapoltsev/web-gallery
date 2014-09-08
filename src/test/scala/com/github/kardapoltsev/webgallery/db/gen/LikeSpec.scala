package com.github.kardapoltsev.webgallery.db.gen


import com.github.kardapoltsev.webgallery.db.Database
import org.scalatest._
import org.joda.time._
import scalikejdbc.scalatest.AutoRollback
import scalikejdbc._

class LikeSpec extends fixture.FlatSpec with Matchers with AutoRollback with FakeDataCreator {
  val l = Like.syntax("l")

  Database.init()
  behavior of "Like"

  it should "find by primary keys" in { implicit session =>
    createLike()
    val maybeFound = Like.find(123)
    maybeFound.isDefined should be(true)
  }
  it should "find all records" in { implicit session =>
    createLike()
    val allResults = Like.findAll()
    allResults.size should be >(0)
  }
  it should "count all records" in { implicit session =>
    createLike()
    val count = Like.countAll()
    count should be >(0L)
  }
  it should "find by where clauses" in { implicit session =>
    createLike()
    val results = Like.findAllBy(sqls.eq(l.id, 123))
    results.size should be >(0)
  }
  it should "count by where clauses" in { implicit session =>
    createLike()
    val count = Like.countBy(sqls.eq(l.id, 123))
    count should be >(0L)
  }
  it should "create new record" in { implicit session =>
    createImage()
    val created = Like.create(imageId = 123, ownerId = 123, createTime = DateTime.now)
    created should not be(null)
  }
  it should "save a record" in { implicit session =>
    createLike()
    val entity = Like.findAll().head
    val modified = entity
    val updated = Like.save(modified.copy(createTime = DateTime.now()))
    updated should not equal(entity)
  }
  it should "destroy a record" in { implicit session =>
    createLike()
    val entity = Like.findAll().head
    Like.destroy(entity)
    val shouldBeNone = Like.find(123)
    shouldBeNone.isDefined should be(false)
  }

}
        