package com.github.kardapoltsev.webgallery


import akka.actor.{ActorLogging, Actor}
import com.github.kardapoltsev.webgallery.http.{SuccessResponse, InternalResponse, ErrorResponse, InternalRequest}
import com.github.kardapoltsev.webgallery.db.AuthType._
import com.github.kardapoltsev.webgallery.db._
import scalikejdbc.{DBSession, DB}
import com.github.kardapoltsev.webgallery.routing.UserManagerRequest
import com.github.kardapoltsev.webgallery.util.Bcrypt


/**
 * Created by alexey on 6/17/14.
 */
class UserManager extends Actor with ActorLogging {
  import com.github.kardapoltsev.webgallery.UserManager._

  
  def receive: Receive = {
    case r: RegisterUser => register(r)
    case r: Auth => auth(r)
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
        if(Bcrypt.check(request.password, hash)){
          sender() ! SuccessResponse
        } else {
          sender() ! ErrorResponse.NotFound
        }
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


object UserManager {
  case class RegisterUser(name: String, authId: String, authType: AuthType, password: String) extends UserManagerRequest
  case class RegisterUserResponse(user: User) extends InternalResponse

  case class Auth(authId: String, authType: AuthType, password: String) extends UserManagerRequest
}
