package com.github.kardapoltsev.webgallery.routing

import akka.actor.{ ActorRef, Props, ActorLogging, Actor }
import akka.event.LoggingReceive
import com.github.kardapoltsev.webgallery._
import com.github.kardapoltsev.webgallery.acl.AclManager
import com.github.kardapoltsev.webgallery.db.ImageId
import com.github.kardapoltsev.webgallery.tags.TagsManager

import scala.reflect.ClassTag

/**
 * Created by alexey on 6/18/14.
 */
class Router extends Actor with ActorLogging {

  val userManager = actor[UserManager]
  val imageManager = actor[ImageManager]
  val aclManager = actor[AclManager]
  val commentManager = actor[CommentManager]
  val tagsManager = actor[TagsManager]

  private def actor[T <: Actor: ClassTag](implicit m: Manifest[T]): ActorRef =
    context.actorOf(Props[T], m.runtimeClass.getSimpleName)

  def receive: Receive = LoggingReceive {
    case msg: UserManagerRequest => userManager forward msg
    case msg: ImageManagerRequest => imageManager forward msg
    case msg: ImageHolderRequest => imageManager forward msg
    case msg: AclManagerRequest => aclManager forward msg
    case msg: CommentManagerRequest => commentManager forward msg
    case msg: TagsManagerRequest => tagsManager forward msg
  }

}

/**
 * All requests will be routed to managers according to this traits
 */
sealed trait Routing
trait UserManagerRequest extends Routing
trait ImageManagerRequest extends Routing
trait AclManagerRequest extends Routing
trait CommentManagerRequest extends Routing
trait TagsManagerRequest extends Routing
trait ImageHolderRequest extends Routing {
  def imageId: ImageId
}
