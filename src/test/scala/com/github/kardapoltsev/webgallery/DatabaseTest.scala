package com.github.kardapoltsev.webgallery


import akka.actor.{Props, ActorSystem}
import akka.testkit.{TestActorRef, ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import com.github.kardapoltsev.webgallery.db.{Metadata, Tag, Image}
import com.github.kardapoltsev.webgallery.Database.{UpdateImageParams, SuccessResponse, UpdateImage}



/**
 * Created by alexey on 6/6/14.
 */
class DatabaseTest (_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll with TestFiles {

  def this() = this(ActorSystem("MySpec"))

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  val database = system.actorOf(Props[Database])

  "Database actor" should {
    "update image" in {
      Database.db.transaction { implicit s =>
        Metadata.insert(dsc2845Metadata)
        Image.insert(dsc2845Image)
      }

      database ! UpdateImage(dsc2845Image.id, UpdateImageParams(Some(Seq(Tag("testTag")))))

      expectMsg(SuccessResponse)

      Database.db.transaction { implicit s =>
        Image.deleteById(dsc2845Image.id)
      }
    }
  }
}