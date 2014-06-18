package com.github.kardapoltsev.webgallery

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterEach, BeforeAndAfterAll, Matchers, WordSpecLike}
import com.github.kardapoltsev.webgallery.db.gen.FakeDataCreator
import com.github.kardapoltsev.webgallery.UserManager.{Auth, RegisterUserResponse, RegisterUser}
import com.github.kardapoltsev.webgallery.db._
import com.github.kardapoltsev.webgallery.http.{ErrorResponse, SuccessResponse}


/**
 * Created by alexey on 6/6/14.
 */
class UserManagerSpec (_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll with BeforeAndAfterEach with TestFiles with FakeDataCreator {

  def this() = this(ActorSystem("MySpec"))

  //init sessions
  Database

  Server.init()

  val router = WebGalleryActorSelection.routerSelection

  override def afterAll(): Unit = {
    Database.cleanDatabase()
    TestKit.shutdownActorSystem(system)
  }


  override def afterEach(): Unit = {
    Database.cleanDatabase()
  }


  "UserManager" should {
    "register user" in {
      router ! RegisterUser("test", "test", AuthType.Direct, "password")
      expectMsgType[RegisterUserResponse]
    }

    "send Conflict for duplicate credentials" in {
      router ! RegisterUser("test", "test2", AuthType.Direct, "password")
      expectMsgType[RegisterUserResponse]
      router ! RegisterUser("test", "test2", AuthType.Direct, "password")
      expectMsg(ErrorResponse.Conflict)
    }

    "authorize user" in {
      router ! RegisterUser("test", "test3", AuthType.Direct, "password")
      expectMsgType[RegisterUserResponse]
      router ! Auth("test3", AuthType.Direct, "password")
      expectMsg(SuccessResponse)
    }

    "send NotFound if password is wrong" in {
      router ! RegisterUser("test", "test4", AuthType.Direct, "password")
      expectMsgType[RegisterUserResponse]
      router ! Auth("test4", AuthType.Direct, "bad password")
      expectMsg(ErrorResponse.NotFound)
    }

    "send NotFound for non-existing user" in {
      router ! Auth("test5", AuthType.Direct, "password")
      expectMsg(ErrorResponse.NotFound)
    }
  }
}
