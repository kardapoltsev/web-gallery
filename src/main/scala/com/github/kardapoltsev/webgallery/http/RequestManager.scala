package com.github.kardapoltsev.webgallery.http

import akka.actor.{ActorLogging, Actor}
import com.github.kardapoltsev.webgallery.WebGalleryActorSelection
import akka.pattern.{ask, pipe}
import com.github.kardapoltsev.webgallery.SessionManager.{GetSessionResponse, GetSession}
import scala.concurrent.Future
import akka.util.Timeout
import akka.event.LoggingReceive

/**
 * Actor between [[RequestDispatcher]] and [[com.github.kardapoltsev.webgallery.routing.Router]] that will check
 * sessions and authorization
 */
class RequestManager extends Actor with ActorLogging {
  import concurrent.duration._
  implicit val requestTimeout = Timeout(5.seconds)
  import context.dispatcher

  private val router = WebGalleryActorSelection.routerSelection
  private val sessionManager = WebGalleryActorSelection.sessionManagerSelection

  def receive: Receive = LoggingReceive {
    case r: AuthorizedRequest =>
      r.sessionId match {
        case Some(sId) =>
          sessionManager ? GetSession(sId) flatMap {
            case GetSessionResponse(Some(session)) => router ? r.withSession(session)
            case _ => Future.successful(ErrorResponse.Unauthorized)
          } pipeTo sender()
        case None => sender() ! ErrorResponse.Unauthorized
      }
    case r: ApiRequest => router forward r
  }
}
