package com.github.kardapoltsev.webgallery.http

import akka.actor.{ActorLogging, Actor}
import com.github.kardapoltsev.webgallery.acl.Permissions
import com.github.kardapoltsev.webgallery.util.Hardcoded
import com.github.kardapoltsev.webgallery.{Configs, WebGalleryActorSelection}
import akka.pattern.{ask, pipe}
import com.github.kardapoltsev.webgallery.SessionManager.{ObtainSessionResponse, ObtainSession, GetSessionResponse, GetSession}
import com.github.kardapoltsev.webgallery.db._
import scala.concurrent.Future
import akka.util.Timeout
import akka.event.LoggingReceive

/**
 * Actor between [[RequestDispatcher]] and [[com.github.kardapoltsev.webgallery.routing.Router]] that will check
 * sessions and authorization
 */
class RequestManager extends Actor with ActorLogging {
  implicit val requestTimeout = Configs.Timeouts.LongRunning
  import context.dispatcher

  private def router = WebGalleryActorSelection.routerSelection
  private def sessionManager = WebGalleryActorSelection.sessionManagerSelection


  def receive: Receive = LoggingReceive {
    case r: ApiRequest =>
      sessionManager ? ObtainSession(r.sessionId) flatMap {
        case ObtainSessionResponse(session) =>
          r match {
            case privileged: PrivilegedRequest =>
              if(isAccessGranted(privileged, session.userId)) {
                router ? r.withSession(session)
              }
              else {
                Future.successful(ErrorResponse.Forbidden)
              }
            case authorized: AuthorizedRequest =>
              session.userId match {
                case Hardcoded.AnonymousUserId =>
                  Future.successful(ErrorResponse.Unauthorized)
                case _ =>
                  router ? r.withSession(session)
              }
            case _ =>
              router ? r.withSession(session)
          }
      } pipeTo sender()
  }


  private def isAccessGranted(r: PrivilegedRequest, requesterId: UserId): Boolean = {
    r.permissions match {
      case Permissions.Write =>
        r.subjectType match {
          case EntityType.Tag => Tag.find(r.subjectId).fold(false)(_.ownerId == requesterId)
          case EntityType.Image => Image.find(r.subjectId).fold(false)(_.ownerId == requesterId)
        }
      case Permissions.Read =>
        r.subjectType match {
          case EntityType.Tag => Acl.existsForTag(r.subjectId, requesterId)
          case EntityType.Image => Acl.existsForImage(r.subjectId, requesterId)
        }
    }
  }

}
