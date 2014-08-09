package com.github.kardapoltsev.webgallery

import akka.actor.{ActorLogging, Actor}
import com.github.kardapoltsev.webgallery.db._
import com.github.kardapoltsev.webgallery.http.{SuccessResponse, PrivilegedRequest, AuthorizedRequest}
import com.github.kardapoltsev.webgallery.routing.AclManagerRequest
import spray.json.DefaultJsonProtocol
import scalikejdbc._

/**
 * Created by alexey on 8/8/14.
 */
class AclManager extends Actor with ActorLogging {
  import AclManager._


  def receive = {
    case r @ GrantAccess(tagId, users) =>
      DB localTx { implicit s =>
        users.foreach { userId =>
          if(!Acl.exists(tagId, userId))
            Acl.create(tagId, userId)
        }
      }
      sender() ! SuccessResponse
    case r @ RevokeAccess(tagId, users) =>
      DB localTx { implicit s =>
        users.foreach { userId =>
          Acl.delete(tagId, userId)
        }
      }
      sender() ! SuccessResponse
    case r @ GetGrantees(tagId) =>
      val users = Acl.findByTagId(tagId).map(acl => User.find(acl.userId)).flatten
      sender() ! GetGranteesResponse(users)
  }

}


trait PrivilegedTagRequest extends PrivilegedRequest {
  def tagId: TagId
  def subjectType = EntityType.Tag
  def subjectId = tagId
}

object AclManager extends DefaultJsonProtocol {
  case class GrantAccess(tagId: TagId, users: Seq[UserId]) extends PrivilegedTagRequest with AclManagerRequest
  object GrantAccess {
    implicit val _ = jsonFormat2(GrantAccess.apply)
  }


  case class RevokeAccess(tagId: TagId, users: Seq[UserId]) extends PrivilegedTagRequest with AclManagerRequest
  object RevokeAccess {
    implicit val _ = jsonFormat2(RevokeAccess.apply)
  }


  case class GetGrantees(tagId: TagId) extends PrivilegedTagRequest with AclManagerRequest
  object GetGrantees {
    implicit val _ = jsonFormat1(GetGrantees.apply)
  }
  case class GetGranteesResponse(users: Seq[User])
  object GetGranteesResponse {
    implicit val _ = jsonFormat1(GetGranteesResponse.apply)
  }

}
