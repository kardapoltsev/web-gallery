package com.github.kardapoltsev.webgallery


import akka.actor.{ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import com.github.kardapoltsev.webgallery.TagsManager.{CreateTag, CreateTagResponse}
import org.scalatest.{BeforeAndAfterEach, BeforeAndAfterAll, Matchers, WordSpecLike}
import com.github.kardapoltsev.webgallery.db._
import com.github.kardapoltsev.webgallery.Database._
import com.github.kardapoltsev.webgallery.processing.{ScaleType, SpecificSize}
import com.github.kardapoltsev.webgallery.http.{ErrorResponse, SuccessResponse}
import scalikejdbc.{DB}


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


  private def sendAddTags(imageId: Int, tags: Seq[TagId]): Unit = {
    withSession { s =>
      router ! Database.AddTags(imageId, tags).withSession(s)
    }
  }

}
