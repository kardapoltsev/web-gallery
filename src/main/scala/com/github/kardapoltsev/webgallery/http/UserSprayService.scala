package com.github.kardapoltsev.webgallery.http

import spray.routing.{Route, HttpService}
import com.github.kardapoltsev.webgallery.UserManager.{Auth, RegisterUserResponse, RegisterUser}

/**
 * Created by alexey on 6/18/14.
 */
trait UserSprayService extends BaseSprayService { this: HttpService =>
  import marshalling._
  import spray.httpx.SprayJsonSupport._
  import spray.http._
  import BaseSprayService._

  def registerUser(r: RegisterUser): Result[RegisterUserResponse]
  def auth(r: Auth): Result[SuccessResponse]

  val usersRoute: Route =
    pathPrefix("api") {
      (path("users") & post) {
        dynamic {
          handleWith {
            registerUser
          }
        }
      } ~
      (path("auth") & post) {
        dynamic {
          handleWith {
            auth
          }
        }
      }
    }

}
