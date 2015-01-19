package com.github.kardapoltsev.webgallery

import akka.actor.{ ActorLogging, Actor }
import com.github.kardapoltsev.webgallery.util.Hardcoded
import db._
import akka.event.LoggingReceive

/**
 * Created by alexey on 6/19/14.
 */
class SessionManager extends Actor with ActorLogging {
  import SessionManager._

  def receive: Receive = LoggingReceive {
    case GetSession(sessionId) =>
      val s = Session.find(sessionId)
      sender ! GetSessionResponse(s)
    case CreateSession(userId, userAgent) =>
      val s = Session.create(userId, userAgent)
      sender ! CreateSessionResponse(s)
    case DeleteSession(sessionId) => Session.delete(sessionId)
    case ObtainSession(sessionId, userAgent) =>
      val session = sessionId flatMap Session.find match {
        case Some(s) => s
        case None => Session.create(Hardcoded.AnonymousUserId, userAgent)
      }
      sender() ! ObtainSessionResponse(session)
  }

}

object SessionManager {
  case class GetSession(sessionId: SessionId)
  case class GetSessionResponse(session: Option[Session])

  /**
   * Get existing session if it exists or create new one with AnonymousUserId
   */
  case class ObtainSession(sessionId: Option[SessionId], userAgent: Option[String])
  case class ObtainSessionResponse(session: Session)

  case class CreateSession(userId: UserId, userAgent: Option[String])
  case class CreateSessionResponse(session: Session)

  case class DeleteSession(sessionId: SessionId)
}
