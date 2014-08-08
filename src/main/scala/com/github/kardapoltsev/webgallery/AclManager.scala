package com.github.kardapoltsev.webgallery

import akka.actor.{ActorLogging, Actor}
import com.github.kardapoltsev.webgallery.db.UserId
import com.github.kardapoltsev.webgallery.http.AuthorizedRequest
import com.github.kardapoltsev.webgallery.routing.AclManagerRequest
import spray.json.DefaultJsonProtocol

/**
 * Created by alexey on 8/8/14.
 */
class AclManager extends Actor with ActorLogging {


  def receive = Actor.emptyBehavior

}


object AclManager extends DefaultJsonProtocol {
  case class GrantAccess(tag: String, users: Seq[UserId]) extends AuthorizedRequest with AclManagerRequest
  object GrantAccess {
    implicit val _ = jsonFormat2(GrantAccess.apply)
  }


  case class RevokeAccess(tag: String, users: Seq[UserId])
}