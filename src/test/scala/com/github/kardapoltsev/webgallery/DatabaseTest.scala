package com.github.kardapoltsev.webgallery


import akka.actor.{Props, ActorSystem}
import akka.testkit.{TestActorRef, ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import com.github.kardapoltsev.webgallery.db.{Metadata, Tag, Image}
import com.github.kardapoltsev.webgallery.Database._
import com.github.kardapoltsev.webgallery.Database.UpdateImage
import com.github.kardapoltsev.webgallery.Database.UpdateImageParams
import com.github.kardapoltsev.webgallery.Database.CreateTag



/**
 * Created by alexey on 6/6/14.
 */
class DatabaseTest (_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll with TestFiles {
  import concurrent.duration._

  def this() = this(ActorSystem("MySpec"))

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
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

        //clean up
        Tag.deleteById(resp.tag.id)
      }
    }
    "update image" in {
      Database.db.transaction { implicit s =>
        Metadata.insert(dsc2845Metadata)
        Image.insert(dsc2845Image)
      }

      database ! UpdateImage(dsc2845Image.id, UpdateImageParams(Some(Seq(CreateTag("testTag")))))

      expectMsg(SuccessResponse)

      Database.db.transaction { implicit s =>
        Image.deleteById(dsc2845Image.id)
      }
    }
  }
}