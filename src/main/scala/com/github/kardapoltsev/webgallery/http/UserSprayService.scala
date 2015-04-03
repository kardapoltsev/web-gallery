package com.github.kardapoltsev.webgallery.http

import spray.routing.{ Route, HttpService }
import com.github.kardapoltsev.webgallery.UserManager._
import shapeless._

/**
 * Created by alexey on 6/18/14.
 */
trait UserSprayService extends BaseSprayService { this: HttpService =>
  import marshalling._
  import BaseSprayService._

  val usersRoute: Route =
    pathPrefix("api") {
      pathPrefix("users") {
        (pathEnd & post) {
          perRequest[RegisterUser, AuthResponse]
        } ~ get {
          path(IntNumber) { userId =>
            perRequest(userId :: HNil) {
              createWrapper[GetUser, GetUserResponse]
            }
          } ~
            path("current") {
              perRequest[GetCurrentUser, GetUserResponse]
            }
        }
      }
    }

}
