package com.github.kardapoltsev.webgallery

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import com.github.kardapoltsev.webgallery.TagsManager._
import com.github.kardapoltsev.webgallery.db._
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Matchers, WordSpecLike}
import scalikejdbc.DB



/**
 * Created by alexey on 6/6/14.
 */
class TagsManagerSpec (_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll with BeforeAndAfterEach with TestFiles with SessionHelper {

  def this() = this(ActorSystem("MySpec"))


  Server.init()

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }


  override def beforeEach(): Unit = {
    DB autoCommit { implicit s =>
      createUser()
    }
  }


  override def afterEach(): Unit = {
    Database.cleanDatabase()
  }



  private val router = WebGalleryActorSelection.routerSelection

  "TagsManager" should {

    "create tag" in {
      sendCreateTag("tag1")
      val resp = expectMsgType[CreateTagResponse]
      resp.tag.name should be("tag1")

      //check database
      val t = Tag.find(userId, resp.tag.name)
      t should be('defined)
    }

    "not create tag twice" in {
      sendCreateTag("tag1")
      expectMsgType[CreateTagResponse]
      sendCreateTag("tag1")
      expectMsgType[CreateTagResponse]

      sendGetTags
      val tags = expectMsgType[GetTagsResponse].tags
      tags should have size 1
    }


    "search tags" in {
      sendCreateTag("searchTag1")
      expectMsgType[CreateTagResponse]
      sendCreateTag("searchTag2")
      expectMsgType[CreateTagResponse]

      router ! SearchTags("sear")
      val result = expectMsgType[GetTagsResponse]

      result.tags should have size 2
    }

    "get tags by user id" in {
      Tag.create(userId, "searchTag1")
      Tag.create(userId, "searchTag2")

      sendGetTags
      val result = expectMsgType[GetTagsResponse]

      result.tags should have size 2
    }


    "return recent tags by user id" in {
      Tag.create(userId, "searchTag1")
      Tag.create(userId, "searchTag2")

      val limit = 1

      withSession { s =>
        router ! GetRecentTags(s.userId).withSession(s).withLimit(limit)
      }

      val result = expectMsgType[GetTagsResponse]

      result.tags should have size limit
    }
  }


  private def sendCreateTag(name: String): Unit = {
    withSession { s =>
      router ! CreateTag(name).withSession(s)
    }
  }


  private def sendGetTags: Unit = {
    withSession { s =>
      router ! GetTags(s.userId).withSession(s)
    }
  }

}
