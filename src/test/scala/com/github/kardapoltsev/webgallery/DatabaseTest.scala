package com.github.kardapoltsev.webgallery


import akka.actor.{Props, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterEach, BeforeAndAfterAll, Matchers, WordSpecLike}
import com.github.kardapoltsev.webgallery.db._
import com.github.kardapoltsev.webgallery.Database._
import com.github.kardapoltsev.webgallery.Database.UpdateImage
import com.github.kardapoltsev.webgallery.Database.GetTagsResponse
import com.github.kardapoltsev.webgallery.Database.CreateImageResponse
import com.github.kardapoltsev.webgallery.Database.UpdateImageParams
import com.github.kardapoltsev.webgallery.Database.CreateTag
import scala.Some
import com.github.kardapoltsev.webgallery.Database.GetImagesResponse
import com.github.kardapoltsev.webgallery.Database.CreateTagResponse
import com.github.kardapoltsev.webgallery.Database.GetImageResponse


/**
 * Created by alexey on 6/6/14.
 */
class DatabaseTest (_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll with BeforeAndAfterEach with TestFiles {

  def this() = this(ActorSystem("MySpec"))

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }


  override def afterEach(): Unit = {
    Database.db.transaction { implicit s =>
      Database.cleanDatabase()
    }
  }


  val database = system.actorOf(Props[Database])

  "Database actor" should {

    "create tag" in {
      database ! CreateTag("tag1")
      val resp = expectMsgType[CreateTagResponse]
      resp.tag.name should be("tag1")

      Database.db.transaction { implicit s =>
        //check database
        val t = Tag.getByName(resp.tag.name)
        t should be('defined)
      }
    }

    "not create tag twice" in {
      database ! CreateTag("tag1")
      expectMsgType[CreateTagResponse]
      database ! CreateTag("tag1")
      expectMsgType[CreateTagResponse]

      database ! GetTags
      val tags = expectMsgType[GetTagsResponse].tags
      tags should have size 1
    }

    "get image by id" in {

      database ! Database.CreateImage(dsc2845Image)
      val id = expectMsgType[CreateImageResponse].id

      database ! Database.GetImage(id)

      val response = expectMsgType[GetImageResponse]
      response.image should be('defined)
    }

    "update image" in {
      Database.db.transaction { implicit s =>
        Metadata.insert(dsc2845Metadata)
        Image.insert(dsc2845Image)
      }

      database ! UpdateImage(dsc2845Image.id, UpdateImageParams(Some(Seq(CreateTag("testTag")))))

      expectMsg(SuccessResponse)
    }

    "find images by tag" in {
      database ! Database.CreateImage(dsc2845Image)
      expectMsgType[CreateImageResponse]

      database ! Database.GetByTag(dsc2845Image.tags.head.name)

      val result = expectMsgType[GetImagesResponse]
      result.images.length should be(1)
    }

    "search tags" in {

      Database.db.transaction { implicit session =>
        val tag = Tag("searchTag1")
        Tag.insert(tag)
        val tag2 = Tag("searchTag2")
        Tag.insert(tag2)
      }

      database ! Database.SearchTags("sear")
      val result = expectMsgType[GetTagsResponse]

      result.tags should have size 2
    }

    "get all tags" in {
      Database.db.transaction { implicit session =>
        val tag = Tag("searchTag1")
        Tag.insert(tag)
        val tag2 = Tag("searchTag2")
        Tag.insert(tag2)
      }

      database ! Database.GetTags
      val result = expectMsgType[GetTagsResponse]

      result.tags should have size 2
    }

    "add tags to image" in {
      database ! Database.CreateImage(dsc2845Image)
      val id = expectMsgType[CreateImageResponse].id

      database ! Database.AddTags(id, Seq("newTag"))
      expectMsg(SuccessResponse)
    }

    "not add tags to non existing image" in {
      database ! Database.AddTags(1, Seq("newTag"))
      expectMsg(ErrorResponse)
    }

    "create alternative" in {
      database ! Database.CreateImage(dsc2845Image)
      val id = expectMsgType[CreateImageResponse].id
      database ! Database.CreateAlternative(Alternative(id, "", TransformImageParams(100, 100, false)))
      expectMsg(SuccessResponse)
    }

    "not create alternative for non existing image" in {
      database ! Database.CreateAlternative(Alternative(0, "", TransformImageParams(100, 100, false)))
      expectMsg(ErrorResponse)
    }

    "find alternative" in {
      database ! Database.CreateImage(dsc2845Image)
      val id = expectMsgType[CreateImageResponse].id
      database ! Database.CreateAlternative(Alternative(id, "", TransformImageParams(100, 100, false)))
      expectMsg(SuccessResponse)

      database ! Database.FindAlternative(id, TransformImageParams(50, 70, false))
      val result = expectMsgType[FindAlternativeResponse]
      result.alternative should be('defined)
    }
  }
}
