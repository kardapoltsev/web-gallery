package com.github.kardapoltsev.webgallery.db.gen

import org.scalatest._
import org.joda.time._
import scalikejdbc.scalatest.AutoRollback
import scalikejdbc._
import java.util.Date



class SessionSpec extends fixture.FlatSpec with Matchers with AutoRollback with FakeDataCreator {
  val s = Session.syntax("s")

  behavior of "Session"

  it should "find by primary keys" in { implicit session =>
    createSession()
    val maybeFound = Session.find(sessionId)
    maybeFound.isDefined should be(true)
  }
  it should "find all records" in { implicit session =>
    createSession()
    val allResults = Session.findAll()
    allResults.size should be >(0)
  }
  it should "count all records" in { implicit session =>
    createSession()
    val count = Session.countAll()
    count should be >(0L)
  }
  it should "find by where clauses" in { implicit session =>
    createSession()
    val results = Session.findAllBy(sqls.eq(s.id, sessionId))
    results.size should be >(0)
  }
  it should "count by where clauses" in { implicit session =>
    createSession()
    val count = Session.countBy(sqls.eq(s.id, sessionId))
    count should be >(0L)
  }
  it should "create new record" in { implicit session =>
    createUser()
    val created = Session.create(userId, DateTime.now())
    created should not be(null)
  }
  it should "save a record" in { implicit session =>
    createSession()
    val entity = Session.findAll().head
    // TODO modify something
    val modified = entity.copy(updateTime = DateTime.now())
    val updated = Session.save(modified)
    updated should not equal(entity)
  }
  it should "destroy a record" in { implicit session =>
    createSession()
    val entity = Session.findAll().head
    Session.destroy(entity)
    val shouldBeNone = Session.find(sessionId)
    shouldBeNone.isDefined should be(false)
  }

}
        