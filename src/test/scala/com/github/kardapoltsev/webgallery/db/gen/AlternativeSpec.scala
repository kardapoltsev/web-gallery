package com.github.kardapoltsev.webgallery.db.gen

import org.scalatest._
import org.joda.time._
import scalikejdbc.scalatest.AutoRollback
import scalikejdbc._

class AlternativeSpec extends fixture.FlatSpec with Matchers with AutoRollback with FakeDataCreator {
  val as = Alternative.syntax("a")

  behavior of "Alternative"

  it should "find by primary keys" in { implicit session =>
    createAlternative()
    val maybeFound = Alternative.find(alternativeId)
    maybeFound.isDefined should be(true)
  }
  it should "find all records" in { implicit session =>
    createAlternative()
    val allResults = Alternative.findAll()
    allResults.size should be >(0)
  }
  it should "count all records" in { implicit session =>
    createAlternative()
    val count = Alternative.countAll()
    count should be >(0L)
  }
  it should "find by where clauses" in { implicit session =>
    createAlternative()
    val results = Alternative.findAllBy(sqls.eq(as.id, alternativeId))
    results.size should be >(0)
  }
  it should "count by where clauses" in { implicit session =>
    createAlternative()
    val count = Alternative.countBy(sqls.eq(as.id, alternativeId))
    count should be >(0L)
  }
  it should "create new record" in { implicit session =>
    createImage()
    val created = Alternative.create(imageId = alternativeId, filename = "MyString", width = Some(123), height = Some(123), scaleType = "MyString")
    created should not be(null)
  }
  it should "save a record" in { implicit session =>
    createAlternative()
    val entity = Alternative.findAll().head
    // TODO modify something
    val modified = entity.copy(width = None)
    val updated = Alternative.save(modified)
    updated should not equal(entity)
  }
  it should "destroy a record" in { implicit session =>
    createAlternative()
    val entity = Alternative.findAll().head
    Alternative.destroy(entity)
    val shouldBeNone = Alternative.find(alternativeId)
    shouldBeNone.isDefined should be(false)
  }

}
        