package com.github.kardapoltsev.webgallery


import akka.actor.{ActorLogging, Actor}
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
    case CreateSession(userId) =>
      val s = Session.create(userId)
      sender ! CreateSessionResponse(s)
  }

}


object SessionManager {
  case class GetSession(sessionId: SessionId)
  case class GetSessionResponse(session: Option[Session])

  case class CreateSession(userId: UserId)
  case class CreateSessionResponse(session: Session)

  case class DeleteSession(sessionId: SessionId)
}
