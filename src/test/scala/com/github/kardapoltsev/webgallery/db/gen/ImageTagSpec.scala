package com.github.kardapoltsev.webgallery.db.gen


import com.github.kardapoltsev.webgallery.db.Database
import org.scalatest._
import scalikejdbc.scalatest.AutoRollback



class ImageTagSpec extends fixture.FlatSpec with Matchers with AutoRollback with FakeDataCreator {
  import scalikejdbc._
  val its = ImageTag.syntax("it")

  behavior of "ImageTag"

  Database.init()


  it should "find by primary keys" in { implicit session =>
    createImage()
    createTag()
    ImageTag.create(imageId, tagId)
    val maybeFound = ImageTag.find(imageId, tagId)
    maybeFound.isDefined should be(true)
  }
  it should "find all records" in { implicit session =>
    createImageTag()
    val allResults = ImageTag.findAll()
    allResults.size should be >(0)
  }
  it should "count all records" in { implicit session =>
    createImageTag()
    val count = ImageTag.countAll()
    count should be >(0L)
  }
  it should "find by where clauses" in { implicit session =>
    createImageTag()
    val results = ImageTag.findAllBy(sqls.eq(its.imageId, imageId))
    results.size should be >(0)
  }
  it should "count by where clauses" in { implicit session =>
    createImageTag()
    val count = ImageTag.countBy(sqls.eq(its.imageId, imageId))
    count should be >(0L)
  }
  it should "create new record" in { implicit session =>
    createImage()
    createTag()
    val created = ImageTag.create(imageId = imageId, tagId = tagId)
    created should not be(null)
  }
  it should "save a record" in { implicit session =>
    createImageTag()
    createImage2()
    val entity = ImageTag.findAll().head
    // TODO modify something
    val modified = entity.copy(imageId = imageId2)
    val updated = ImageTag.save(modified)
    updated should not equal(entity)
  }
  it should "destroy a record" in { implicit session =>
    createImageTag()
    val entity = ImageTag.findAll().head
    ImageTag.destroy(entity)
    val shouldBeNone = ImageTag.find(imageId, tagId)
    shouldBeNone.isDefined should be(false)
  }

}
        