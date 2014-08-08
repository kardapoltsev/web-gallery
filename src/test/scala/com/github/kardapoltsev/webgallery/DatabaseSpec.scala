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
import com.github.kardapoltsev.webgallery.Database.GetImagesResponse
import com.github.kardapoltsev.webgallery.Database.CreateTagResponse
import com.github.kardapoltsev.webgallery.Database.GetImageResponse
import com.github.kardapoltsev.webgallery.processing.{ScaleType, SpecificSize}
import com.github.kardapoltsev.webgallery.http.{ErrorResponse, SuccessResponse}
import com.github.kardapoltsev.webgallery.db.gen.FakeDataCreator
import scalikejdbc.{DB, AutoSession}
import org.joda.time.DateTime


/**
 * Created by alexey on 6/6/14.
 */
class DatabaseSpec (_system: ActorSystem) extends TestKit(_system) with ImplicitSender
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

  "Database actor" should {

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

    "get image by id" in {
      sendCreateImage(dsc2845Image)
      val id = expectMsgType[CreateImageResponse].image.id

      router ! Database.GetImage(id)

      val response = expectMsgType[GetImageResponse]
      response.image.id should be(id)
    }

    "update image" in {
      Image.create(dsc2845Image.name, dsc2845Image.filename, dsc2845Image.ownerId)

      withSession { s =>
        router ! UpdateImage(dsc2845Image.id, UpdateImageParams(Some(Seq("testTag")))).withSession(s)
      }

      expectMsg(SuccessResponse)
    }

    "find images by tag" in {
      sendCreateImage(dsc2845Image)
      val id = expectMsgType[CreateImageResponse].image.id
      sendCreateTag("tag")
      val tagId = expectMsgType[CreateTagResponse].tag.id
      sendAddTags(id, Seq(tagId))
      expectMsg(SuccessResponse)

      withSession { s =>
        router ! Database.GetByTag(tagId).withSession(s)
      }

      val result = expectMsgType[GetImagesResponse]
      result.images.length should be(1)
    }

    "search tags" in {
      sendCreateTag("searchTag1")
      expectMsgType[CreateTagResponse]
      sendCreateTag("searchTag2")
      expectMsgType[CreateTagResponse]

      router ! Database.SearchTags("sear")
      val result = expectMsgType[GetTagsResponse]

      result.tags should have size 2
    }

    "get all tags" in {
      Tag.create(userId, "searchTag1")
      Tag.create(userId, "searchTag2")

      sendGetTags
      val result = expectMsgType[GetTagsResponse]

      result.tags should have size 2
    }

    "add tags to image" in {
      sendCreateImage(dsc2845Image)
      val id = expectMsgType[CreateImageResponse].image.id
      sendCreateTag("tag")
      val tagId = expectMsgType[CreateTagResponse].tag.id
      sendAddTags(id, Seq(tagId))

      expectMsg(SuccessResponse)
    }

    "not add tags to non existing image" in {
      sendCreateTag("tag")
      val tagId = expectMsgType[CreateTagResponse].tag.id
      sendAddTags(-1, Seq(tagId))
      expectMsg(ErrorResponse.NotFound)
    }

    "create alternative" in {
      sendCreateImage(dsc2845Image)
      val id = expectMsgType[CreateImageResponse].image.id
      router ! Database.CreateAlternative(id, "", SpecificSize(100, 100, ScaleType.FitSource))
      expectMsgType[CreateAlternativeResponse]
    }

    "not create alternative for non existing image" in {
      router ! Database.CreateAlternative(0, "", SpecificSize(100, 100, ScaleType.FitSource))
      expectMsg(ErrorResponse.NotFound)
    }

    "find alternative" in {
      sendCreateImage(dsc2845Image)
      val id = expectMsgType[CreateImageResponse].image.id
      router ! Database.CreateAlternative(id, "", SpecificSize(100, 100, ScaleType.FitSource))
      expectMsgType[CreateAlternativeResponse]

      router ! Database.FindAlternative(id, SpecificSize(50, 70, ScaleType.FitSource))
      val result = expectMsgType[FindAlternativeResponse]
      result.alternative should be('defined)
    }
  }


  private def sendCreateImage(img: Image): Unit = {
    router ! Database.CreateImage(img.ownerId, img.name, img.filename, None, Seq.empty)
  }


  private def sendCreateTag(name: String): Unit = {
    withSession { s =>
      router ! CreateTag(name).withSession(s)
    }
  }


  private def sendGetTags: Unit = {
    withSession { s =>
      router ! GetTags.withSession(s)
    }
  }


  private def sendAddTags(imageId: Int, tags: Seq[TagId]): Unit = {
    withSession { s =>
      router ! Database.AddTags(imageId, tags).withSession(s)
    }
  }

}
