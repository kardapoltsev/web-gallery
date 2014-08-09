package com.github.kardapoltsev.webgallery.routing

import akka.actor.{Props, ActorLogging, Actor}
import com.github.kardapoltsev.webgallery.{AclManager, Database, ImageProcessor, UserManager}

/**
 * Created by alexey on 6/18/14.
 */
class Router extends Actor with ActorLogging {
  import com.github.kardapoltsev.webgallery.util.Hardcoded.ActorNames

  val userManager = context.actorOf(Props[UserManager], ActorNames.UserManager)
  val imageProcessor = context.actorOf(Props[ImageProcessor], ActorNames.ImageProcessor)
  val database = context.actorOf(Props[Database], ActorNames.Database)
  val aclManager = context.actorOf(Props[AclManager], ActorNames.AclManager)


  def receive: Receive = {
    case msg: UserManagerRequest => userManager forward msg
    case msg: DatabaseRequest => database forward msg
    case msg: ImageProcessorRequest => imageProcessor forward msg
    case msg: AclManagerRequest => aclManager forward msg
  }

}


/**
 * All requests will be routed to managers according to this traits
 */
sealed trait Routing
trait UserManagerRequest extends Routing
trait DatabaseRequest extends Routing
trait ImageProcessorRequest extends Routing
trait AclManagerRequest extends Routing
