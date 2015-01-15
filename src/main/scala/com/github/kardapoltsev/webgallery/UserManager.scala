package com.github.kardapoltsev.webgallery

import java.io.{ FileOutputStream, File }
import java.util.UUID

import akka.actor.{ Props, ActorLogging, Actor }
import com.github.kardapoltsev.webgallery.db.AuthType.AuthType
import com.github.kardapoltsev.webgallery.es.{ UserCreated, EventPublisher }
import com.github.kardapoltsev.webgallery.http._
import com.github.kardapoltsev.webgallery.db._
import com.github.kardapoltsev.webgallery.oauth.{ VKService }
import com.github.kardapoltsev.webgallery.util.Hardcoded.ActorNames
import scalikejdbc.{ DBSession, DB }
import com.github.kardapoltsev.webgallery.routing.UserManagerRequest
import com.github.kardapoltsev.webgallery.util.{ Bcrypt }
import spray.json.DefaultJsonProtocol
import com.github.kardapoltsev.webgallery.SessionManager.{ CreateSessionResponse, CreateSession }
import akka.pattern.{ ask, pipe }
import scala.concurrent.Future
import akka.event.LoggingReceive

import scala.util.control.NonFatal

/**
 * Created by alexey on 6/17/14.
 */
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
  case class AuthResponse(userId: UserId, sessionId: SessionId) extends ApiResponse
  object AuthResponse {
    implicit val _ = jsonFormat2(AuthResponse.apply)
  }

  case class GetUser(userId: UserId) extends AuthorizedRequest with UserManagerRequest

  case class GetCurrentUser() extends AuthorizedRequest with UserManagerRequest
  case class GetUserResponse(user: User) extends ApiResponse
  object GetUserResponse {
    implicit val _ = jsonFormat1(GetUserResponse.apply)
  }

  case class SearchUsers(query: String) extends AuthorizedRequest with UserManagerRequest with Pagination
  case class SearchUsersResponse(users: Seq[User]) extends ApiResponse
  case object SearchUsersResponse {
    implicit val _ = jsonFormat1(SearchUsersResponse.apply)
  }

  case class SetUserAvatar(userId: UserId, imageId: ImageId) extends UserManagerRequest
}

class UserManager extends Actor with ActorLogging with EventPublisher {
  import com.github.kardapoltsev.webgallery.UserManager._
  import com.github.kardapoltsev.webgallery.http.marshalling._
  import context.dispatcher

  private val sessionManager = WebGalleryActorSelection.sessionManagerSelection
  import context.dispatcher
  implicit val requestTimeout = Configs.Timeouts.LongRunning
  private val vkService = context.actorOf(Props[VKService], ActorNames.VKService)

  def receive: Receive = LoggingReceive(processSearchUser orElse processSetUserAvatar orElse processGetUser orElse {
    case r: RegisterUser => register(r)
    case r: Auth => auth(r)
    case r: VKAuth => vkAuth(r)
    case r: GetCurrentUser => processGetUser(GetUser(r.session.get.userId).withContext(r.ctx.get))
  })

  private def processSetUserAvatar: Receive = {
    case SetUserAvatar(userId, imageId) =>
      User.setAvatar(userId, imageId)
  }

  import spray.client.pipelining._
  import spray.http._

  private def vkAuth(r: VKAuth) {

    vkService ? VKService.GetToken(r.code) map {
      case response @ VKService.GetTokenResponse(token, expire, userId) =>
        Credentials.find(userId.toString, AuthType.VK) match {
          case Some(cred) =>
            successAuth(cred.userId, r)
          case None =>
            vkService ? VKService.GetUserInfo(userId.toString) map {
              case VKService.GetUserInfoResponse(Seq(userInfo)) =>
                register(
                  RegisterUser(
                    userInfo.first_name + " " + userInfo.last_name, response.user_id.toString, AuthType.VK, None
                  ).withContext(r.ctx.get)
                )
            }
        }
    } recover {
      case NonFatal(e) =>
        log.error(e, "error retrieving vk access token")
        r.complete(ErrorResponse.ServiceUnavailable)
    }
  }

  private def processSearchUser: Receive = {
    case r @ SearchUsers(query) =>
      val users = DB readOnly { implicit s =>
        User.search(query, r.session.get.userId, r.offset, r.limit)
      }
      r.complete(SearchUsersResponse(users))
  }

  private def processGetUser: Receive = {
    case r @ GetUser(userId) =>
      User.find(userId) match {
        case Some(user) => r.complete(GetUserResponse(user))
        case None => r.complete(ErrorResponse.NotFound)
      }
  }

  private def auth(request: Auth): Unit = {
    Credentials.find(request.authId, request.authType) match {
      case None => request.complete(ErrorResponse.NotFound)
      case Some(credentials) => AuthType.withName(credentials.authType) match {
        case AuthType.Direct => directAuth(request, credentials)
      }
    }
  }

  private def directAuth(request: Auth, credentials: Credentials): Unit = {
    credentials.passwordHash match {
      case None => request.complete(ErrorResponse.BadRequest)
      case Some(hash) =>
        if (Bcrypt.check(request.password, hash)) {
          successAuth(credentials.userId, request)
        } else {
          request.complete(ErrorResponse.NotFound)
        }
    }
  }

  private def successAuth(userId: UserId, r: ApiRequest): Unit = {
    createSession(userId) map { s =>
      r.complete(AuthResponse(userId, s.id))
    }
  }

  private def createSession(userId: UserId): Future[Session] = {
    sessionManager ? CreateSession(userId) map {
      case CreateSessionResponse(session) => session
    }
  }

  private def register(request: RegisterUser): Unit = {
    DB localTx { implicit s =>
      Credentials.find(request.authId, request.authType) match {
        case Some(_) => request.complete(ErrorResponse.Conflict)
        case None =>
          val passwordHash = request.password map Bcrypt.create
          val user = User.create(request.name)
          Credentials.create(request.authId, request.authType.toString, passwordHash, user.id)
          s.connection.commit()

          downloadAvatar(request.authType, request.authId, user)
          publish(UserCreated(user))
          successAuth(user.id, request)
      }
    }
  }

  private def downloadAvatar(authType: AuthType, authId: String, user: User): Unit = {
    authType match {
      case AuthType.VK => vkService ? VKService.GetUserInfo(authId, Seq("photo_max_orig")) foreach {
        case VKService.GetUserInfoResponse(Seq(u)) =>
          downloadAvatar(u.photo_max_orig.get, user)
      }
      case _ => //nothing to do more
    }
  }

  private def downloadAvatar(url: String, user: User): Unit = {
    log.debug(s"downloading avatar from $url for $user")
    val pipe = sendReceive
    pipe(Get(url)) onSuccess {
      case response =>
        val file = saveFile(response.entity)
        val image = Image.create(file.getName, file.getName, user.id)
        User.save(user.copy(avatarId = image.id))
    }
  }

  private def saveFile(entity: HttpEntity): File = {
    val filename = Configs.OriginalsDir + UUID.randomUUID().toString + ".jpg"
    val file = new File(filename)
    file.createNewFile()
    val out = new FileOutputStream(file)
    out.write(entity.data.toByteArray)
    out.close()
    file
  }
}
