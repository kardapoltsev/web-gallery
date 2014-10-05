package com.github.kardapoltsev.webgallery.tags


import akka.actor.{ActorLogging, Actor}
import com.github.kardapoltsev.webgallery.db.{Image, Tag}
import com.github.kardapoltsev.webgallery.es.{ImageEvent, ImageUntagged, ImageTagged}
import com.github.kardapoltsev.webgallery.util.Hardcoded



/**
 * Created by alexey on 9/9/14.
 */
private class EventListener extends Actor with ActorLogging {

  override def preStart(): Unit = {
    super.preStart()
    context.system.eventStream.subscribe(self, classOf[ImageEvent])
  }

  def receive: Receive = processImageTagged orElse processImageUntagged



  private def processImageTagged: Receive = {
    case ImageTagged(image, tag) =>
      if(tag.coverId == Hardcoded.DefaultCoverId) {
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

}
