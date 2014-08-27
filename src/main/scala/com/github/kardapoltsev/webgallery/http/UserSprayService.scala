package com.github.kardapoltsev.webgallery.http

import spray.routing.{Route, HttpService}
import com.github.kardapoltsev.webgallery.UserManager._
import shapeless._


/**
 * Created by alexey on 6/18/14.
 */
trait UserSprayService extends BaseSprayService { this: HttpService =>
  import marshalling._
  import spray.http._
  import BaseSprayService._

  protected def registerUser(r: RegisterUser): Result[AuthResponse] = processRequest(r)
  protected def getUser(r: GetUser): Result[GetUserResponse] = processRequest(r)
  protected def getCurrentUser(r: GetCurrentUser): Result[GetUserResponse] = processRequest(r)

  val usersRoute: Route =
    pathPrefix("api") {
      pathPrefix("users") {
        (pathEnd & post) {
          dynamic {
            handleWith {
              registerUser
            }
          }
        } ~ get {
          path(IntNumber) { userId =>
            dynamic {
              handleWith(userId :: HNil)(getUser)
            }
          } ~
          path("current") {
            dynamic {
              handleWith(getCurrentUser)
            }
          }
        }
      }
    }

}
