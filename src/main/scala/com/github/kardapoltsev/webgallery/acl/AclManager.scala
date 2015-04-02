package com.github.kardapoltsev.webgallery.acl

import akka.actor.{ Actor, ActorLogging }
import com.github.kardapoltsev.webgallery.db._
import com.github.kardapoltsev.webgallery.http.{ ErrorResponse, ApiResponse, PrivilegedRequest, SuccessResponse }
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
          if (!Acl.existsForTag(tagId, userId))
            Acl.create(tagId, userId)
        }
      }
      sender() ! SuccessResponse
    case r @ RevokeAccess(tagId, users) =>
      DB localTx { implicit s =>
        Tag.find(tagId) match {
          case Some(t) =>
            users filterNot (_ == t.ownerId) foreach { userId =>
              Acl.delete(tagId, userId)
            }
            sender() ! SuccessResponse
          case None =>
            sender() ! ErrorResponse.NotFound
        }
      }
    case r @ GetGrantees(tagId) =>
      val users = Acl.findByTagId(tagId).filterNot(_.userId == r.requesterId).map(acl => User.find(acl.userId)).flatten
      sender() ! GetGranteesResponse(users)
  }

}

trait PrivilegedTagRequest extends PrivilegedRequest {
  def tagId: TagId
  def subjectType = EntityType.Tag
  def subjectId = tagId
}

