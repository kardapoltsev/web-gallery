package com.github.kardapoltsev.webgallery

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import com.github.kardapoltsev.webgallery.AclManager.{GetGranteesResponse, GetGrantees, RevokeAccess, GrantAccess}
import com.github.kardapoltsev.webgallery.db.Acl
import com.github.kardapoltsev.webgallery.db.gen.FakeDataCreator
import com.github.kardapoltsev.webgallery.http.SuccessResponse
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Matchers, WordSpecLike}
import scalikejdbc.AutoSession



/**
 * Created by alexey on 6/6/14.
 */
class AclManagerSpec (_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll with BeforeAndAfterEach with TestFiles with FakeDataCreator {

  def this() = this(ActorSystem("MySpec"))

  //init sessions
  Database
  implicit val session = AutoSession

  Server.init()

  val router = WebGalleryActorSelection.routerSelection

  override def afterAll(): Unit = {
    Database.cleanDatabase()
    TestKit.shutdownActorSystem(system)
  }


  override def afterEach(): Unit = {
    Database.cleanDatabase()
  }


  "AclManager" should {
    "grand access" in {
      createUser()
      createUser2()
      createTag
      router ! GrantAccess(tagId, Seq(userId2))
      expectMsgType[SuccessResponse]
      Acl.findByTagId(tagId).length should be(1)
    }

    "not grant access twice" in {
      createUser()
      createUser2()
      createTag
      router ! GrantAccess(tagId, Seq(userId2))
      expectMsgType[SuccessResponse]
      router ! GrantAccess(tagId, Seq(userId2))
      expectMsgType[SuccessResponse]
      Acl.findByTagId(tagId).length should be(1)
    }

    "revoke access" in {
      createUser()
      createUser2()
      createTag
      router ! GrantAccess(tagId, Seq(userId2))
      expectMsgType[SuccessResponse]
      router ! RevokeAccess(tagId, Seq(userId2))
      expectMsgType[SuccessResponse]
      Acl.findByTagId(tagId).length should be(0)
    }

    "return grantees" in {
      createUser()
      createUser2()
      createTag
      router ! GrantAccess(tagId, Seq(userId2))
      expectMsgType[SuccessResponse]

      router ! GetGrantees(tagId)
      val response = expectMsgType[GetGranteesResponse]
      response.users.length should be(1)
    }
  }
}
