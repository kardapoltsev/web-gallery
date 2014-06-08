package com.github.kardapoltsev.webgallery.db.gen

import org.scalatest._
import org.joda.time._
import scalikejdbc.scalatest.AutoRollback
import scalikejdbc._

class ImageSpec extends fixture.FlatSpec with Matchers with AutoRollback with FakeDataCreator {
  val i = Image.syntax("i")

  behavior of "Image"

  it should "find by primary keys" in { implicit session =>
    createImage()
    val maybeFound = Image.find(imageId)
    maybeFound.isDefined should be(true)
  }
  it should "find all records" in { implicit session =>
    createImage()
    val allResults = Image.findAll()
    allResults.size should be >(0)
  }
  it should "count all records" in { implicit session =>
    createImage()
    val count = Image.countAll()
    count should be >(0L)
  }
  it should "find by where clauses" in { implicit session =>
    createImage()
    val results = Image.findAllBy(sqls.eq(i.id, imageId))
    results.size should be >(0)
  }
  it should "count by where clauses" in { implicit session =>
    createImage()
    val count = Image.countBy(sqls.eq(i.id, imageId))
    count should be >(0L)
  }
  it should "create new record" in { implicit session =>
    val created = Image.create(name = "MyString", filename = "MyString")
    created should not be(null)
  }
  it should "save a record" in { implicit session =>
    createImage()
    val entity = Image.findAll().head
    // TODO modify something
    val modified = entity.copy(filename = "newFilename")
    val updated = Image.save(modified)
    updated should not equal(entity)
  }
  it should "destroy a record" in { implicit session =>
    createImage()
    val entity = Image.findAll().head
    Image.destroy(entity)
    val shouldBeNone = Image.find(imageId)
    shouldBeNone.isDefined should be(false)
  }

}
        