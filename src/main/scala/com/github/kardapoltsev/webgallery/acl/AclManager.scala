package com.github.kardapoltsev.webgallery.acl

import akka.actor.{Actor, ActorLogging}
import com.github.kardapoltsev.webgallery.db._
import com.github.kardapoltsev.webgallery.http.{ApiResponse, PrivilegedRequest, SuccessResponse}
import com.github.kardapoltsev.webgallery.routing.AclManagerRequest
import scalikejdbc._
import spray.json.DefaultJsonProtocol

/**
 * Created by alexey on 8/8/14.
 */
object AclManager extends DefaultJsonProtocol {
  case class GrantAccess(tagId: TagId, users: Seq[UserId]) extends PrivilegedTagRequest with AclManagerRequest {
    def permissions = Permissions.Write
  }


  case class RevokeAccess(tagId: TagId, users: Seq[UserId]) extends PrivilegedTagRequest with AclManagerRequest {
    def permissions = Permissions.Write
  }


  case class GetGrantees(tagId: TagId) extends PrivilegedTagRequest with AclManagerRequest {
    def permissions = Permissions.Write
  }
  case class GetGranteesResponse(users: Seq[User]) extends ApiResponse
  object GetGranteesResponse {
    implicit val _ = jsonFormat1(GetGranteesResponse.apply)
  }

}


class AclManager extends Actor with ActorLogging {
  import com.github.kardapoltsev.webgallery.acl.AclManager._
  import com.github.kardapoltsev.webgallery.http.marshalling._
  import context.dispatcher


  def receive = {
    case r @ GrantAccess(tagId, users) =>
      DB localTx { implicit s =>
        users.foreach { userId =>
          if(!Acl.existsForTag(tagId, userId))
            Acl.create(tagId, userId)
        }
      }
      r.complete(SuccessResponse)
    case r @ RevokeAccess(tagId, users) =>
      DB localTx { implicit s =>
        users.foreach { userId =>
          Acl.delete(tagId, userId)
        }
      }
      r.complete(SuccessResponse)
    case r @ GetGrantees(tagId) =>
      val users = Acl.findByTagId(tagId).map(acl => User.find(acl.userId)).flatten
      r.complete(GetGranteesResponse(users))
  }

}


trait PrivilegedTagRequest extends PrivilegedRequest {
  def tagId: TagId
  def subjectType = EntityType.Tag
  def subjectId = tagId
}

