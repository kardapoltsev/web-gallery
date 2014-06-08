package com.github.kardapoltsev.webgallery.db.gen

import org.scalatest._
import org.joda.time._
import scalikejdbc.scalatest.AutoRollback
import scalikejdbc._

class MetadataSpec extends fixture.FlatSpec with Matchers with AutoRollback with FakeDataCreator {
  val m = Metadata.syntax("m")

  behavior of "Metadata"

  it should "find by primary keys" in { implicit session =>
    createMetadata()
    val maybeFound = Metadata.find(metadataId)
    maybeFound.isDefined should be(true)
  }
  it should "find all records" in { implicit session =>
    createMetadata()
    val allResults = Metadata.findAll()
    allResults.size should be >(0)
  }
  it should "count all records" in { implicit session =>
    createMetadata()
    val count = Metadata.countAll()
    count should be >(0L)
  }
  it should "find by where clauses" in { implicit session =>
    createMetadata()
    val results = Metadata.findAllBy(sqls.eq(m.id, metadataId))
    results.size should be >(0)
  }
  it should "count by where clauses" in { implicit session =>
    createMetadata()
    val count = Metadata.countBy(sqls.eq(m.id, metadataId))
    count should be >(0L)
  }
  it should "create new record" in { implicit session =>
    createImage()
    val created = Metadata.create(imageId = metadataId)
    created should not be(null)
  }
  it should "save a record" in { implicit session =>
    createMetadata()
    val entity = Metadata.findAll().head
    // TODO modify something
    val modified = entity.copy(creationTime = Some(DateTime.now()))
    val updated = Metadata.save(modified)
    updated should not equal(entity)
  }
  it should "destroy a record" in { implicit session =>
    createMetadata()
    val entity = Metadata.findAll().head
    Metadata.destroy(entity)
    val shouldBeNone = Metadata.find(metadataId)
    shouldBeNone.isDefined should be(false)
  }

}
        