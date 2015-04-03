package com.github.kardapoltsev.webgallery.http

import com.github.kardapoltsev.webgallery.SessionManager.DeleteSession
import com.github.kardapoltsev.webgallery.UserManager.{ VKAuth, AuthResponse, Auth }
import com.github.kardapoltsev.webgallery.WebGalleryActorSelection
import com.github.kardapoltsev.webgallery.http.BaseSprayService.Result
import com.github.kardapoltsev.webgallery.util.Hardcoded
import shapeless.HNil
import spray.http.StatusCodes.Redirection
import spray.http.{ StatusCodes, HttpHeaders, HttpResponse }
import spray.routing.HttpService

/**
 * Created by alexey on 8/8/14.
 */
trait AuthSprayService extends BaseSprayService { this: HttpService =>

  import marshalling._
  private lazy val sessionManager = WebGalleryActorSelection.sessionManagerSelection

  val authRoute =
    pathPrefix("api") {
      pathPrefix("auth") {
        (pathEnd & post) {
          perRequest[Auth, AuthResponse]
        } ~
          pathPrefix("callback") {
            (path("vk") & parameter('code)) { code =>
              perRequest(code :: HNil) {
                r: VKAuth => HandlerWrapper[AuthResponse](r)
              }
            }
          }
      } ~
        path("logout") {
          cookie(Hardcoded.CookieName) { sessionId =>
            deleteCookie(Hardcoded.CookieName) {
              sessionManager ! DeleteSession(sessionId.content)
              redirect("/", StatusCodes.Found)
            }
          }
        }
    }
}
