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
import com.github.kardapoltsev.webgallery.processing.{ScaleType, SpecificSize}



/**
 * Created by alexey on 6/6/14.
 */
class DatabaseTest (_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll with BeforeAndAfterEach with TestFiles {

  def this() = this(ActorSystem("MySpec"))

  Database

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }


  override def afterEach(): Unit = {
    Database.cleanDatabase()
  }


  def createImage(img: Image): Unit = {
    database ! Database.CreateImage(img.name, img.filename, None, Seq.empty)
  }


  val database = system.actorOf(Props[Database])

  "Database actor" should {

    "create tag" in {
      database ! CreateTag("tag1")
      val resp = expectMsgType[CreateTagResponse]
      resp.tag.name should be("tag1")

      //check database
      val t = Tag.find(resp.tag.name)
      t should be('defined)
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
      createImage(dsc2845Image)
      val id = expectMsgType[CreateImageResponse].image.id

      database ! Database.GetImage(id)

      val response = expectMsgType[GetImageResponse]
      response.image should be('defined)
    }

    "update image" in {
      Image.create(dsc2845Image.name, dsc2845Image.filename)

      database ! UpdateImage(dsc2845Image.id, UpdateImageParams(Some(Seq(CreateTag("testTag")))))

      expectMsg(SuccessResponse)
    }

    "find images by tag" in {
      createImage(dsc2845Image)
      val id = expectMsgType[CreateImageResponse].image.id
      database ! Database.AddTags(id, Seq("tag"))
      expectMsg(SuccessResponse)

      database ! Database.GetByTag("tag")

      val result = expectMsgType[GetImagesResponse]
      result.images.length should be(1)
    }

    "search tags" in {
      database ! Database.CreateTag("searchTag1")
      expectMsgType[CreateTagResponse]
      database ! Database.CreateTag("searchTag2")
      expectMsgType[CreateTagResponse]

      database ! Database.SearchTags("sear")
      val result = expectMsgType[GetTagsResponse]

      result.tags should have size 2
    }

    "get all tags" in {
      Tag.create("searchTag1")
      Tag.create("searchTag2")

      database ! Database.GetTags
      val result = expectMsgType[GetTagsResponse]

      result.tags should have size 2
    }

    "add tags to image" in {
      createImage(dsc2845Image)
      val id = expectMsgType[CreateImageResponse].image.id

      database ! Database.AddTags(id, Seq("newTag"))
      expectMsg(SuccessResponse)
    }

    "not add tags to non existing image" in {
      database ! Database.AddTags(1, Seq("newTag"))
      expectMsg(ErrorResponse)
    }

    "create alternative" in {
      createImage(dsc2845Image)
      val id = expectMsgType[CreateImageResponse].image.id
      database ! Database.CreateAlternative(id, "", SpecificSize(100, 100, ScaleType.FitSource))
      expectMsgType[CreateAlternativeResponse]
    }

    "not create alternative for non existing image" in {
      database ! Database.CreateAlternative(0, "", SpecificSize(100, 100, ScaleType.FitSource))
      expectMsg(ErrorResponse)
    }

    "find alternative" in {
      createImage(dsc2845Image)
      val id = expectMsgType[CreateImageResponse].image.id
      database ! Database.CreateAlternative(id, "", SpecificSize(100, 100, ScaleType.FitSource))
      expectMsgType[CreateAlternativeResponse]

      database ! Database.FindAlternative(id, SpecificSize(50, 70, ScaleType.FitSource))
      val result = expectMsgType[FindAlternativeResponse]
      result.alternative should be('defined)
    }
  }
}
