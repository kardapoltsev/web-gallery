package com.github.kardapoltsev.webgallery.http

import akka.actor.{ActorLogging, Actor}
import com.github.kardapoltsev.webgallery.{Configs, WebGalleryActorSelection}
import akka.pattern.{ask, pipe}
import com.github.kardapoltsev.webgallery.SessionManager.{GetSessionResponse, GetSession}
import com.github.kardapoltsev.webgallery.db.{Image, UserId, Tag, EntityType}
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

  private val router = WebGalleryActorSelection.routerSelection
  private val sessionManager = WebGalleryActorSelection.sessionManagerSelection

  def receive: Receive = LoggingReceive {
    case r: AuthorizedRequest =>
      r.sessionId match {
        case Some(sId) =>
          sessionManager ? GetSession(sId) flatMap {
            case GetSessionResponse(Some(session)) =>
              r match {
                case privileged: PrivilegedRequest =>
                  if(isAccessGranted(privileged, session.userId))
                    router ? r.withSession(session)
                  else
                    Future.successful(ErrorResponse.Forbidden)
                case _ =>
                  router ? r.withSession(session)
              }
            case _ => Future.successful(ErrorResponse.Unauthorized)
          } pipeTo sender()
        case None => sender() ! ErrorResponse.Unauthorized
      }
    case r: ApiRequest => router forward r
  }


  private def isAccessGranted(r: PrivilegedRequest, requesterId: UserId): Boolean = {
    r.subjectType match {
      case EntityType.Tag => Tag.find(r.subjectId).fold(false)(_.ownerId == requesterId)
      case EntityType.Image => Image.find(r.subjectId).fold(false)(_.ownerId == requesterId)
    }
  }

}
