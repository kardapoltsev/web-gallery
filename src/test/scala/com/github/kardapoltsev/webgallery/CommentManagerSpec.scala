package com.github.kardapoltsev.webgallery

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import com.github.kardapoltsev.webgallery.CommentManager.{GetComments, GetCommentsResponse, AddCommentResponse, AddComment}
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
  with WordSpecLike with Matchers with BeforeAndAfterAll with BeforeAndAfterEach with FakeDataCreator
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
    "get comments" in {
      createImage()
      val r = addComment()
      addComment(Some(r.comment.id))
      addComment(Some(r.comment.id))
      val comments = getComments().comments
      comments.size should be(1)
      comments.head.replies.size should be(2)
    }
  }


  private def addComment(parentId: Option[Int] = None): AddCommentResponse = {
    withSession { s =>
      router ! AddComment(imageId, "comment text", parentId).withSession(s)
    }
    expectMsgType[AddCommentResponse]
  }


  private def getComments(): GetCommentsResponse = {
    withSession { s =>
      router ! GetComments(imageId)
      expectMsgType[GetCommentsResponse]
    }
  }

}
