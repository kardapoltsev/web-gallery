package com.github.kardapoltsev.webgallery.db.gen

import org.scalatest._
import org.joda.time._
import scalikejdbc.scalatest.AutoRollback
import scalikejdbc._

class CredentialsSpec extends fixture.FlatSpec with Matchers with AutoRollback with FakeDataCreator {
  val c = Credentials.syntax("c")

  behavior of "Credentials"

  it should "find by primary keys" in { implicit session =>
    createCredentials()
    val maybeFound = Credentials.find(credentialsId)
    maybeFound.isDefined should be(true)
  }
  it should "find all records" in { implicit session =>
    createCredentials()
    val allResults = Credentials.findAll()
    allResults.size should be >(0)
  }
  it should "count all records" in { implicit session =>
    createCredentials()
    val count = Credentials.countAll()
    count should be >(0L)
  }
  it should "find by where clauses" in { implicit session =>
    createCredentials()
    val results = Credentials.findAllBy(sqls.eq(c.id, credentialsId))
    results.size should be >(0)
  }
  it should "count by where clauses" in { implicit session =>
    createCredentials()
    val count = Credentials.countBy(sqls.eq(c.id, credentialsId))
    count should be >(0L)
  }
  it should "create new record" in { implicit session =>
    createUser()
    val created = Credentials.create(authId = "MyString", authType = "MyString", userId = userId)
    created should not be(null)
  }
  it should "save a record" in { implicit session =>
    createCredentials()
    val entity = Credentials.findAll().head
    // TODO modify something
    val modified = entity.copy(authType = "VK")
    val updated = Credentials.save(modified)
    updated should not equal(entity)
  }
  it should "destroy a record" in { implicit session =>
    createCredentials()
    val entity = Credentials.findAll().head
    Credentials.destroy(entity)
    val shouldBeNone = Credentials.find(123)
    shouldBeNone.isDefined should be(false)
  }

}
        