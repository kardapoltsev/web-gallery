package com.github.kardapoltsev.webgallery.db.gen


import com.github.kardapoltsev.webgallery.Database
import org.scalatest._
import org.joda.time._
import scalikejdbc.scalatest.AutoRollback
import scalikejdbc._

class SettingsSpec extends fixture.FlatSpec with Matchers with AutoRollback with FakeDataCreator {
  val s = Settings.syntax("s")

  Database.init()

  behavior of "Settings"

  it should "find by primary keys" in { implicit session =>
    createSettings()
    val maybeFound = Settings.find(settingsId)
    maybeFound.isDefined should be(true)
  }
  it should "find all records" in { implicit session =>
    createSettings()
    val allResults = Settings.findAll()
    allResults.size should be >(0)
  }
  it should "count all records" in { implicit session =>
    createSettings()
    val count = Settings.countAll()
    count should be >(0L)
  }
  it should "find by where clauses" in { implicit session =>
    createSettings()
    val results = Settings.findAllBy(sqls.eq(s.id, settingsId))
    results.size should be >(0)
  }
  it should "count by where clauses" in { implicit session =>
    createSettings()
    val count = Settings.countBy(sqls.eq(s.id, settingsId))
    count should be >(0L)
  }
  it should "create new record" in { implicit session =>
    val created = Settings.create(version = settingsId)
    created should not be(null)
  }
  it should "save a record" in { implicit session =>
    createSettings()
    val entity = Settings.findAll().head
    val modified = entity.copy(version = 100500)
    val updated = Settings.save(modified)
    updated should not equal(entity)
  }
  it should "destroy a record" in { implicit session =>
    createSettings()
    val entity = Settings.findAll().head
    Settings.destroy(entity)
    val shouldBeNone = Settings.find(entity.id)
    shouldBeNone.isDefined should be(false)
  }

}
        