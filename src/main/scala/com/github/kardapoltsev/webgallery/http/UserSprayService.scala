package com.github.kardapoltsev.webgallery.http

import spray.routing.{Route, HttpService}
import com.github.kardapoltsev.webgallery.UserManager._
import shapeless._


/**
 * Created by alexey on 6/18/14.
 */
trait UserSprayService extends BaseSprayService { this: HttpService =>
  import spray.httpx.SprayJsonSupport._
  import marshalling._
  import spray.http._
  import BaseSprayService._

  protected def registerUser(r: RegisterUser): Result[RegisterUserResponse]
  protected def auth(r: Auth): Result[AuthResponse]
  protected def getUser(r: GetUser): Result[GetUserResponse]

  val usersRoute: Route =
    pathPrefix("api") {
      pathPrefix("users") {
        (pathEnd & post) {
          dynamic {
            handleWith {
              registerUser
            }
          }
        } ~
        (path(IntNumber) & get) { userId =>
          dynamic {
            handleWith(userId :: HNil)(getUser)
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
