package com.github.kardapoltsev.webgallery


import akka.actor.{ActorLogging, Actor}
import com.github.kardapoltsev.webgallery.db.AuthType.AuthType
import com.github.kardapoltsev.webgallery.http._
import com.github.kardapoltsev.webgallery.db._
import com.github.kardapoltsev.webgallery.oauth.VK
import scalikejdbc.{DBSession, DB}
import com.github.kardapoltsev.webgallery.routing.UserManagerRequest
import com.github.kardapoltsev.webgallery.util.{Hardcoded, Bcrypt}
import spray.json.DefaultJsonProtocol
import com.github.kardapoltsev.webgallery.SessionManager.{CreateSessionResponse, GetSessionResponse, CreateSession}
import akka.pattern.{ask, pipe}
import scala.concurrent.Future
import akka.util.Timeout
import akka.event.LoggingReceive

import scala.util.control.NonFatal


/**
 * Created by alexey on 6/17/14.
 */

class UserManager extends Actor with ActorLogging {
  import com.github.kardapoltsev.webgallery.UserManager._

  private val sessionManager = WebGalleryActorSelection.sessionManagerSelection
  import context.dispatcher
  import concurrent.duration._
  implicit val requestTimeout = Timeout(20.seconds)

  
  def receive: Receive = LoggingReceive {
    case r: RegisterUser => register(r) pipeTo sender()
    case r: Auth => auth(r)
    case r: VKAuth => vkAuth(r)
    case GetUser(userId) => processGetUser(userId)
    case r @ GetCurrentUser => processGetUser(r.session.get.userId)
    case r @ SearchUsers(query) => processSearchUser(query, r.session.get.userId)
  }


  import spray.client.pipelining._
  import spray.http._
  import spray.httpx.SprayJsonSupport._
  val getVKAuthToken: HttpRequest => Future[VK.GetTokenResponse] = sendReceive ~> unmarshal[VK.GetTokenResponse]
  val getVKUserInfo: HttpRequest => Future[VK.GetUserInfoResponse] = sendReceive ~> unmarshal[VK.GetUserInfoResponse]
  private def vkAuth(r: VKAuth) {
    val getToken = HttpRequest(HttpMethods.GET, Uri("https://oauth.vk.com/access_token").withQuery(
      "client_id" -> Hardcoded.VK.AppId,
      "client_secret"  -> Hardcoded.VK.AppSecret,
      "code" -> r.code,
      "redirect_uri" -> Hardcoded.VK.RedirectUri
    ))

    getVKAuthToken(getToken) flatMap {
      case response =>
        log.debug(response.toString)

        Credentials.find(response.user_id.toString, AuthType.VK) match {
          case Some(cred) =>
            successAuth(cred.userId)
          case None =>
            val getUserInfoRequest = HttpRequest(HttpMethods.GET, Uri("https://api.vkontakte.ru/method/users.get.json")
              .withQuery(
              "uids" -> response.user_id.toString,
              "access_token" -> response.access_token
              )
            )
            getVKUserInfo(getUserInfoRequest) flatMap {
              users =>
                val userInfo = users.response.head
                register(RegisterUser(
                  userInfo.first_name + " " + userInfo.last_name, response.user_id.toString, AuthType.VK, None
                ))
            }
        }

    } recover {
      case NonFatal(e) =>
        log.error(e, "error retrieving vk access token")
        ErrorResponse.ServiceUnavailable
    } pipeTo sender()
  }


  /**
   * Will search on [[gen.User.name]] field
   * @param requesterId used to exclude requester from results
   */
  private def processSearchUser(query: String, requesterId: UserId): Unit = {
    val users = User.search(query, requesterId)
    sender() ! SearchUsersResponse(users)
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
        case AuthType.Direct => directAuth(request, credentials)
      }
    }
  }


  private def directAuth(request: Auth, credentials: Credentials): Unit = {
    credentials.passwordHash match {
      case None => sender() ! ErrorResponse.BadRequest
      case Some(hash) =>
        if(Bcrypt.check(request.password, hash)) {
          successAuth(credentials.userId) pipeTo sender()
        } else {
          sender() ! ErrorResponse.NotFound
        }
    }
  }


  private def successAuth(userId: UserId): Future[AuthResponse] = {
    log.debug(s"success auth for userId: $userId")
    createSession(userId) map { s =>
      AuthResponse(s.id)
    }
  }


  private def createSession(userId: UserId): Future[Session] = {
    sessionManager ? CreateSession(userId) map {
      case CreateSessionResponse(session) => session
    }
  }


  private def register(request: RegisterUser): Future[ApiResponse] = {
    DB localTx { implicit s =>
      Credentials.find(request.authId, request.authType) match {
        case Some(_) => Future.successful(ErrorResponse.Conflict)
        case None =>
          val passwordHash = request.password map Bcrypt.create
          val user = User.create(request.name)
          Credentials.create(
            request.authId, request.authType.toString, passwordHash, user.id)
          s.connection.commit()
          successAuth(user.id)
      }
    }
  }

}


object UserManager extends DefaultJsonProtocol {
  case class RegisterUser(name: String, authId: String, authType: AuthType, password: Option[String])
    extends ApiRequest with UserManagerRequest
  object RegisterUser {
    implicit val registerUserJF = jsonFormat4(RegisterUser.apply)
  }

  
  case class VKAuth(code: String) extends ApiRequest with UserManagerRequest
  case class Auth(authId: String, authType: AuthType, password: String) extends ApiRequest with UserManagerRequest
  object Auth {
    implicit val _ = jsonFormat3(Auth.apply)
  }
  case class AuthResponse(sessionId: SessionId) extends ApiResponse
  object AuthResponse {
    implicit val _ = jsonFormat1(AuthResponse.apply)
  }


  case class GetUser(userId: UserId) extends AuthorizedRequest with UserManagerRequest
  object GetUser {
    implicit val _ = jsonFormat1(GetUser.apply)
  }
  object GetCurrentUser extends AuthorizedRequest with UserManagerRequest
  case class GetUserResponse(user: User) extends ApiResponse
  object GetUserResponse {
    implicit val _ = jsonFormat1(GetUserResponse.apply)
  }
  
  
  case class SearchUsers(query: String) extends AuthorizedRequest with UserManagerRequest
  case class SearchUsersResponse(users: Seq[User])
  case object SearchUsersResponse {
    implicit val _ = jsonFormat1(SearchUsersResponse.apply)
  }
  
}
