package com.github.kardapoltsev.webgallery


import akka.actor.{ActorLogging, Actor}
import com.github.kardapoltsev.webgallery.http._
import com.github.kardapoltsev.webgallery.db.AuthType._
import com.github.kardapoltsev.webgallery.db._
import scalikejdbc.{DBSession, DB}
import com.github.kardapoltsev.webgallery.routing.UserManagerRequest
import com.github.kardapoltsev.webgallery.util.Bcrypt
import spray.json.DefaultJsonProtocol
import com.github.kardapoltsev.webgallery.SessionManager.{CreateSessionResponse, GetSessionResponse, CreateSession}
import akka.pattern.{ask, pipe}
import scala.concurrent.Future
import akka.util.Timeout
import akka.event.LoggingReceive
import com.github.kardapoltsev.webgallery.SessionManager.CreateSession
import scala.Some
import com.github.kardapoltsev.webgallery.SessionManager.CreateSessionResponse


/**
 * Created by alexey on 6/17/14.
 */
class UserManager extends Actor with ActorLogging {
  import com.github.kardapoltsev.webgallery.UserManager._

  private val sessionManager = WebGalleryActorSelection.sessionManagerSelection
  import context.dispatcher
  import concurrent.duration._
  implicit val requestTimeout = Timeout(5.seconds)

  
  def receive: Receive = LoggingReceive {
    case r: RegisterUser => register(r)
    case r: Auth => auth(r)
    case GetUser(userId) => processGetUser(userId)
  }


  private def processGetUser(userId: UserId): Unit = {
    User.find(userId) match {
      case Some(user) => sender() ! GetUserResponse(user)
      case None => sender() ! ErrorResponse.NotFound
    }
  }


  private def auth(request: Auth): Unit = {
    Credentials.find(request.authId, request.authType) match {
      case None => sender() ! ErrorResponse.NotFound
      case Some(credentials) => AuthType.withName(credentials.authType) match {
        case Direct => directAuth(request, credentials)
      }
    }
  }


  private def directAuth(request: Auth, credentials: Credentials): Unit = {
    credentials.passwordHash match {
      case None => sender() ! ErrorResponse.BadRequest
      case Some(hash) =>
        if(Bcrypt.check(request.password, hash)) {
          successAuth(credentials.userId)
        } else {
          sender() ! ErrorResponse.NotFound
        }
    }
  }


  private def successAuth(userId: UserId): Unit = {
    log.debug(s"success auth $userId")
    createSession(userId) map { s =>
      AuthResponse(s.id)
    } pipeTo sender()
  }


  private def createSession(userId: UserId): Future[Session] = {
    sessionManager ? CreateSession(userId) map {
      case CreateSessionResponse(session) => session
    }
  }


  private def register(request: RegisterUser): Unit = {
    DB localTx { implicit s =>
      Credentials.find(request.authId, request.authType) match {
        case Some(_) => sender() ! ErrorResponse.Conflict
        case None =>
          request.authType match {
            case Direct => sender() ! RegisterUserResponse(directRegistration(request))
          }
      }
    }
  }


  private def directRegistration(request: RegisterUser)(implicit session: DBSession): User = {
    val user = User.create(request.name)
    Credentials.create(request.authId, request.authType.toString, Some(Bcrypt.create(request.password)), user.id)
    user
  }
  
}


object UserManager extends DefaultJsonProtocol {
  case class RegisterUser(name: String, authId: String, authType: AuthType, password: String)
    extends ApiRequest with UserManagerRequest
  object RegisterUser {
    implicit val registerUserJF = jsonFormat4(RegisterUser.apply)
  }
  case class RegisterUserResponse(user: User) extends ApiResponse
  object RegisterUserResponse {
    implicit val _ = jsonFormat1(RegisterUserResponse.apply)
  }


  case class Auth(authId: String, authType: AuthType, password: String) extends ApiRequest with UserManagerRequest
  object Auth {
    implicit val _ = jsonFormat3(Auth.apply)
  }
  case class AuthResponse(sessionId: Int) extends ApiResponse
  object AuthResponse {
    implicit val _ = jsonFormat1(AuthResponse.apply)
  }


  case class GetUser(userId: UserId) extends AuthorizedRequest with UserManagerRequest
  object GetUser {
    implicit val _ = jsonFormat1(GetUser.apply)
  }
  case class GetUserResponse(user: User) extends ApiResponse
  object GetUserResponse {
    implicit val _ = jsonFormat1(GetUserResponse.apply)
  }
}
