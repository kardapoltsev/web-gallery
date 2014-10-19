package com.github.kardapoltsev.webgallery.tags


import akka.actor.{ActorLogging, Actor}
import com.github.kardapoltsev.webgallery.db._
import com.github.kardapoltsev.webgallery.es._
import com.github.kardapoltsev.webgallery.util.{MetadataExtractor, Hardcoded}
import scalikejdbc.{DBSession, DB}



/**
 * Created by alexey on 9/9/14.
 */
trait EventListener extends Actor with EventPublisher {
  import com.github.kardapoltsev.webgallery.util.Hardcoded.Tags._

  override def preStart(): Unit = {
    super.preStart()
    context.system.eventStream.subscribe(self, classOf[ImageEvent])
    context.system.eventStream.subscribe(self, classOf[UserEvent])
  }


  protected def handleEvents =
    Seq(processImageTagged, processImageUntagged, processImageCreated, processUserCreated) reduceLeft (_ orElse _)


  private def processImageCreated: Receive = {
    case ImageCreated(image, meta) =>
      meta foreach { m =>
        DB localTx { implicit s =>
          val untagged = Tag.find(image.ownerId, Untagged).get
          val all = Tag.find(image.ownerId, All).get
          val tags = MetadataExtractor.extractTags(m).map(createTag(_, image.ownerId)) match {
            case seq if seq.isEmpty => Seq(untagged, all)
            case seq => seq :+ all
          }
          addTags(image, tags)
        }
      }
  }


  private def processImageTagged: Receive = {
    case ImageTagged(image, tag) =>
      if(!tag.manualCover) {
        Tag.setCoverId(tag.id, image.id, false)
      }
  }


  private def processImageUntagged: Receive = {
    case ImageUntagged(image, tag) =>
      if(tag.coverId == image.id) {
        val newCoverId = Image.findByTag(tag.id, 0, 1).headOption.map(_.id).getOrElse(Hardcoded.DefaultCoverId)
        Tag.setCoverId(tag.id, newCoverId, false)
      }
  }


  private def processUserCreated: Receive = {
    case UserCreated(user) =>
      DB localTx { implicit s =>
        val untagged = Tag.create(user.id, Untagged, system = true, auto = true)
        Acl.create(untagged.id, user.id)
        val all = Tag.create(user.id, All, system = true, auto = true)
        Acl.create(all.id, user.id)
      }
  }



  private def addTags(image: Image, tags: Seq[Tag])(implicit s: DBSession): Unit = {
    tags.foreach { tag =>
      ImageTag.find(image.id, tag.id) match {
        case Some(it) =>
        case None =>
          ImageTag.create(image.id, tag.id)
          publish(ImageTagged(image, tag))
      }
    }
  }


  protected def createTag(name: String, ownerId: UserId)(implicit s: DBSession): Tag
}
