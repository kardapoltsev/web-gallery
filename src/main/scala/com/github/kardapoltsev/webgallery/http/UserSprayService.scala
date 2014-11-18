package com.github.kardapoltsev.webgallery.http

import spray.routing.{ Route, HttpService }
import com.github.kardapoltsev.webgallery.UserManager._
import shapeless._

/**
 * Created by alexey on 6/18/14.
 */
trait UserSprayService extends BaseSprayService { this: HttpService =>
  import marshalling._
  import spray.http._
  import BaseSprayService._

  protected def registerUser(r: RegisterUser) = processRequest(r)
  protected def getUser(r: GetUser) = processRequest(r)
  protected def getCurrentUser(r: GetCurrentUser) = processRequest(r)

  val usersRoute: Route =
    pathPrefix("api") {
      pathPrefix("users") {
        (pathEnd & post) {
          dynamic {
            handleRequest {
              registerUser
            }
          }
        } ~ get {
          path(IntNumber) { userId =>
            dynamic {
              handleRequest(userId :: HNil)(getUser)
            }
          } ~
            path("current") {
              dynamic {
                handleRequest(getCurrentUser)
              }
            }
        }
      }
    }

}
