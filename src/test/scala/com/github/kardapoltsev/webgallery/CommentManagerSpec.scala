package com.github.kardapoltsev.webgallery

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import com.github.kardapoltsev.webgallery.CommentManager.{AddCommentResponse, AddComment}
import com.github.kardapoltsev.webgallery.UserManager._
import com.github.kardapoltsev.webgallery.db._
import com.github.kardapoltsev.webgallery.db.gen.FakeDataCreator
import com.github.kardapoltsev.webgallery.http.{SuccessResponse, ErrorResponse}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Matchers, WordSpecLike}
import scalikejdbc.AutoSession



/**
 * Created by alexey on 6/6/14.
 */
class CommentManagerSpec (_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll with BeforeAndAfterEach with TestFiles with FakeDataCreator
  with SessionHelper {

  def this() = this(ActorSystem("MySpec"))

  //init sessions
  Database
  implicit val dbSession = AutoSession
  Server.init()

  val router = WebGalleryActorSelection.routerSelection

  override def afterAll(): Unit = {
    Database.cleanDatabase()
    TestKit.shutdownActorSystem(system)
  }


  override def afterEach(): Unit = {
    Database.cleanDatabase()
  }


  "CommentManager" should {
    "add comment" in {
      createImage()
      addComment()
    }
  }


  private def addComment(): AddCommentResponse = {
    withSession{ s =>
      router ! AddComment(imageId, "comment text", None).withSession(s)
    }
    expectMsgType[AddCommentResponse]
  }
}
