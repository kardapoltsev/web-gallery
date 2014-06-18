package com.github.kardapoltsev.webgallery


import akka.actor.{ActorLogging, Actor}
import com.github.kardapoltsev.webgallery.http.{InternalResponse, ErrorResponse, InternalRequest}
import com.github.kardapoltsev.webgallery.db.AuthType._
import com.github.kardapoltsev.webgallery.db._
import scalikejdbc.{DBSession, DB}
import com.github.kardapoltsev.webgallery.routing.UserManagerRequest


/**
 * Created by alexey on 6/17/14.
 */
class UserManager extends Actor with ActorLogging {
  import com.github.kardapoltsev.webgallery.UserManager._

  
  def receive: Receive = {
    case r @ RegisterUser(_, authId, authType, _) =>
      DB localTx { implicit s =>
          Credentials.find(authId, authType) match {
            case Some(_) => sender() ! ErrorResponse.Conflict
            case None =>
              authType match {
                case Direct => sender() ! RegisterUserResponse(directRegistration(r))
              }
          }
      }
  }


  def directRegistration(request: RegisterUser)(implicit session: DBSession): User = {
    val user = User.create(request.name)
    Credentials.create(request.authId, request.authType.toString, user.id)
    user
  }
  
}


object UserManager {
  case class RegisterUser(name: String, authId: String, authType: AuthType, password: String) extends UserManagerRequest
  case class RegisterUserResponse(user: User) extends InternalResponse
}
