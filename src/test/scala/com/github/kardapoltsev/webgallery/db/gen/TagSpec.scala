package com.github.kardapoltsev.webgallery.db.gen


import com.github.kardapoltsev.webgallery.util.Hardcoded
import org.scalatest._
import org.joda.time._
import scalikejdbc.scalatest.AutoRollback
import scalikejdbc._

class TagSpec extends fixture.FlatSpec with Matchers with AutoRollback with FakeDataCreator {
  val t = Tag.syntax("t")

  behavior of "Tag"

  it should "find by primary keys" in { implicit session =>
    createTag()
    val maybeFound = Tag.find(tagId)
    maybeFound.isDefined should be(true)
  }
  it should "find all records" in { implicit session =>
    createTag()
    val allResults = Tag.findAll()
    allResults.size should be >(0)
  }
  it should "count all records" in { implicit session =>
    createTag()
    val count = Tag.countAll()
    count should be >(0L)
  }
  it should "find by where clauses" in { implicit session =>
    createTag()
    val results = Tag.findAllBy(sqls.eq(t.id, tagId))
    results.size should be >(0)
  }
  it should "count by where clauses" in { implicit session =>
    createTag()
    val count = Tag.countBy(sqls.eq(t.id, tagId))
    count should be >(0L)
  }
  it should "create new record" in { implicit session =>
    createUser()
    val created = Tag.create(ownerId = userId, name = "MyString", DateTime.now(), Hardcoded.DefaultCoverId)
    created should not be(null)
  }
  it should "save a record" in { implicit session =>
    createTag()
    val entity = Tag.findAll().head
    // TODO modify something
    val modified = entity.copy(name = "newName")
    val updated = Tag.save(modified)
    updated should not equal(entity)
  }
  it should "destroy a record" in { implicit session =>
    createTag()
    val entity = Tag.findAll().head
    Tag.destroy(entity)
    val shouldBeNone = Tag.find(tagId)
    shouldBeNone.isDefined should be(false)
  }

}
        