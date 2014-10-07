package com.github.kardapoltsev.webgallery.tags


import akka.actor.{ActorLogging, Actor}
import com.github.kardapoltsev.webgallery.db.{Image, Tag}
import com.github.kardapoltsev.webgallery.es._
import com.github.kardapoltsev.webgallery.util.Hardcoded
import scalikejdbc.DB



/**
 * Created by alexey on 9/9/14.
 */
trait EventListener extends Actor {

  override def preStart(): Unit = {
    super.preStart()
    context.system.eventStream.subscribe(self, classOf[ImageEvent])
    context.system.eventStream.subscribe(self, classOf[UserEvent])
  }


  protected def processImageTagged: Receive = {
    case ImageTagged(image, tag) =>
      if(tag.coverId == Hardcoded.DefaultCoverId) {
        Tag.setCoverId(tag.id, image.id, false)
      }
  }


  protected def processImageUntagged: Receive = {
    case ImageUntagged(image, tag) =>
      if(tag.coverId == image.id) {
        val newCoverId = Image.findByTag(tag.id, 0, 1).headOption.map(_.id).getOrElse(Hardcoded.DefaultCoverId)
        Tag.setCoverId(tag.id, newCoverId, false)
      }
  }


  protected def processUserCreated: Receive = {
    case UserCreated(user) =>
      import com.github.kardapoltsev.webgallery.util.Hardcoded.Tags._
      DB localTx { implicit s =>
        Tag.create(user.id, Untagged, system = true, auto = true)
        Tag.create(user.id, All, system = true, auto = true)
      }
  }

}
