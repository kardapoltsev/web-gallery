package com.github.kardapoltsev.webgallery

import akka.actor.{ ActorRefFactory, Actor }

/**
 * Created by alexey on 5/28/14.
 */
object WebGalleryActorSelection {
  import com.github.kardapoltsev.webgallery.util.Hardcoded.ActorNames
  private def userActorSelection(path: String)(implicit factory: ActorRefFactory) =
    factory.actorSelection(s"/user/$path")

  def routerSelection(implicit factory: ActorRefFactory) = userActorSelection(ActorNames.Router)
  def sessionManagerSelection(implicit factory: ActorRefFactory) = userActorSelection(ActorNames.SessionManager)

}
