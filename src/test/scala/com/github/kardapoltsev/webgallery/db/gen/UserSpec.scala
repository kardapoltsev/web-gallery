package com.github.kardapoltsev.webgallery.db.gen


import com.github.kardapoltsev.webgallery.db.Database
import com.github.kardapoltsev.webgallery.util.Hardcoded
import org.scalatest._
import org.joda.time._
import scalikejdbc.scalatest.AutoRollback
import scalikejdbc._

class UserSpec extends fixture.FlatSpec with Matchers with AutoRollback with FakeDataCreator {
  val u = User.syntax("u")

  Database.init()

  behavior of "User"

  it should "find by primary keys" in { implicit session =>
    createUser()
    val maybeFound = User.find(userId)
    maybeFound.isDefined should be(true)
  }
  it should "find all records" in { implicit session =>
    createUser()
    val allResults = User.findAll()
    allResults.size should be >(0)
  }
  it should "count all records" in { implicit session =>
    createUser()
    val count = User.countAll()
    count should be >(0L)
  }
  it should "find by where clauses" in { implicit session =>
    createUser()
    val results = User.findAllBy(sqls.eq(u.id, userId))
    results.size should be >(0)
  }
  it should "count by where clauses" in { implicit session =>
    createUser()
    val count = User.countBy(sqls.eq(u.id, userId))
    count should be >(0L)
  }
  it should "create new record" in { implicit session =>
    val created = User.create(name = "MyString", avatarId = Hardcoded.DefaultAvatarId, DateTime.now())
    created should not be(null)
  }
  it should "save a record" in { implicit session =>
    createUser()
    val entity = User.findAll().head
    // TODO modify something
    val modified = entity.copy(name = "newName")
    val updated = User.save(modified)
    updated should not equal(entity)
  }
  it should "destroy a record" in { implicit session =>
    createUser()
    val entity = User.findAll().head
    User.destroy(entity)
    val shouldBeNone = User.find(entity.id)
    shouldBeNone.isDefined should be(false)
  }

}
        