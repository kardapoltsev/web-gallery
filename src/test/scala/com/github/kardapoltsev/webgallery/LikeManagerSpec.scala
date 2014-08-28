package com.github.kardapoltsev.webgallery

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import com.github.kardapoltsev.webgallery.LikeManager.{UnlikeImage, LikeImage}
import com.github.kardapoltsev.webgallery.db.gen.FakeDataCreator
import com.github.kardapoltsev.webgallery.http.{ErrorResponse, SuccessResponse}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Matchers, WordSpecLike}
import scalikejdbc.AutoSession



/**
 * Created by alexey on 6/6/14.
 */
class LikeManagerSpec (_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll with BeforeAndAfterEach with FakeDataCreator
  with SessionHelper {

  def this() = this(ActorSystem("MySpec"))

  //init sessions
  Database
  implicit val dbSession = AutoSession
  Server.init()

  val router = WebGalleryActorSelection.routerSelection

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }


  override def afterEach(): Unit = {
    Database.cleanDatabase()
  }


  "LikeManager" should {
    "like" in {
      createImage()
      createLike()
      expectMsgType[SuccessResponse]
    }

    "unlike" in {
      createImage()
      createLike()
      expectMsgType[SuccessResponse]
      unlike()
      expectMsgType[SuccessResponse]
    }

    "not like unexisting image" in {
      createLike()
      expectMsg(ErrorResponse.UnprocessableEntity)
    }
  }


  private def unlike(): Unit = {
    withSession { s =>
      router ! UnlikeImage(imageId).withSession(s)
    }
  }


  private def createLike(): Unit = {
    withSession { s =>
      router ! LikeImage(imageId).withSession(s)
    }
  }

}
