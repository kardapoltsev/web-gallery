package com.github.kardapoltsev.webgallery

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterEach, BeforeAndAfterAll, Matchers, WordSpecLike}
import com.github.kardapoltsev.webgallery.db.gen.FakeDataCreator
import com.github.kardapoltsev.webgallery.UserManager.{AuthResponse, Auth}
import com.github.kardapoltsev.webgallery.db._
import com.github.kardapoltsev.webgallery.http.SuccessResponse
import com.github.kardapoltsev.webgallery.SessionManager._
import scalikejdbc.AutoSession



/**
 * Created by alexey on 6/6/14.
 */
class SessionManagerSpec (_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll with BeforeAndAfterEach with TestFiles with FakeDataCreator {

  def this() = this(ActorSystem("MySpec"))

  //init sessions
  Database

  Server.init()

  val sessionManager = WebGalleryActorSelection.sessionManagerSelection

  override def afterAll(): Unit = {
    Database.cleanDatabase()
    TestKit.shutdownActorSystem(system)
  }


  "SessionManager" should {
    "create session" in {
      createUser()(AutoSession)
      sessionManager ! CreateSession(userId)
      expectMsgType[CreateSessionResponse]
    }

    "find session" in {
      createUser()(AutoSession)
      sessionManager ! CreateSession(userId)
      val r = expectMsgType[CreateSessionResponse]
      sessionManager ! GetSession(r.session.id)
      val r2 = expectMsgType[GetSessionResponse]
      r2.session.isDefined should be(true)
    }

    "delete session" in {
      createUser()(AutoSession)
      sessionManager ! CreateSession(userId)
      val r = expectMsgType[CreateSessionResponse]
      sessionManager ! GetSession(r.session.id)
      val r2 = expectMsgType[GetSessionResponse]
      r2.session.isDefined should be(true)
      sessionManager ! DeleteSession(r.session.id)
      sessionManager ! GetSession(r.session.id)
      val r3 = expectMsgType[GetSessionResponse]
      r3.session.isDefined should be(false)
    }
  }
}
