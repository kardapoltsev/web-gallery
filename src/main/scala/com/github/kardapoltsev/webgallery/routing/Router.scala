package com.github.kardapoltsev.webgallery.routing

import akka.actor.{ActorRef, Props, ActorLogging, Actor}
import com.github.kardapoltsev.webgallery._

import scala.reflect.ClassTag

/**
 * Created by alexey on 6/18/14.
 */
class Router extends Actor with ActorLogging {

  val userManager = actor[UserManager]
  val imageProcessor = actor[ImageManager]
  val database = actor[Database]
  val aclManager = actor[AclManager]
  val commentManager = actor[CommentManager]
  val tagsManager = actor[TagsManager]
  val likesManager = actor[LikeManager]

  private def actor[T <: Actor : ClassTag](implicit m: Manifest[T]): ActorRef =
    context.actorOf(Props[T], m.runtimeClass.getSimpleName)

  def receive: Receive = {
    case msg: UserManagerRequest => userManager forward msg
    case msg: DatabaseRequest => database forward msg
    case msg: ImageProcessorRequest => imageProcessor forward msg
    case msg: AclManagerRequest => aclManager forward msg
    case msg: CommentManagerRequest => commentManager forward msg
    case msg: TagsManagerRequest => tagsManager forward msg
    case msg: LikeManagerRequest => likesManager forward msg
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
trait CommentManagerRequest extends Routing
trait TagsManagerRequest extends Routing
trait LikeManagerRequest extends Routing
