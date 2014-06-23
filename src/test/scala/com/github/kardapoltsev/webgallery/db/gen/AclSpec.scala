package com.github.kardapoltsev.webgallery.db.gen

import org.scalatest._
import org.joda.time._
import scalikejdbc.scalatest.AutoRollback
import scalikejdbc._

class AclSpec extends fixture.FlatSpec with Matchers with AutoRollback with FakeDataCreator {
  val as = Acl.syntax("a")

  behavior of "Acl"

  it should "find by primary keys" in { implicit session =>
    createAcl()
    val maybeFound = Acl.find(aclId)
    maybeFound.isDefined should be(true)
  }
  it should "find all records" in { implicit session =>
    createAcl()
    val allResults = Acl.findAll()
    allResults.size should be >(0)
  }
  it should "count all records" in { implicit session =>
    createAcl()
    val count = Acl.countAll()
    count should be >(0L)
  }
  it should "find by where clauses" in { implicit session =>
    createAcl()
    val results = Acl.findAllBy(sqls.eq(as.id, aclId))
    results.size should be >(0)
  }
  it should "count by where clauses" in { implicit session =>
    createAcl()
    val count = Acl.countBy(sqls.eq(as.id, aclId))
    count should be >(0L)
  }
  it should "create new record" in { implicit session =>
    createUser()
    createTag()
    val created = Acl.create(tagId = aclId, userId = aclId)
    created should not be(null)
  }
  it should "destroy a record" in { implicit session =>
    createAcl()
    val entity = Acl.findAll().head
    Acl.destroy(entity)
    val shouldBeNone = Acl.find(aclId)
    shouldBeNone.isDefined should be(false)
  }

}
        