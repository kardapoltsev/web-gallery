package com.github.kardapoltsev.webgallery

import akka.actor.Actor

/**
 * Created by alexey on 5/28/14.
 */
trait WebGalleryActorSelection { this: Actor =>
  import Server.Names
  protected def userActorSelection(path: String) = context.actorSelection(s"/user/$path")
  protected def databaseSelection = userActorSelection(Names.Database)
  protected def imageProcessorSelection = userActorSelection(Names.ImageProcessor)
}
