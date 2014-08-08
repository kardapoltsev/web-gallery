package com.github.kardapoltsev.webgallery.http


import com.github.kardapoltsev.webgallery.SessionManager.DeleteSession
import com.github.kardapoltsev.webgallery.UserManager.{VKAuth, AuthResponse, Auth}
import com.github.kardapoltsev.webgallery.WebGalleryActorSelection
import com.github.kardapoltsev.webgallery.http.BaseSprayService.Result
import com.github.kardapoltsev.webgallery.util.Hardcoded
import shapeless.HNil
import spray.http.StatusCodes.Redirection
import spray.http.{StatusCodes, HttpHeaders, HttpResponse}
import spray.routing.HttpService



/**
 * Created by alexey on 8/8/14.
 */
trait AuthSprayService extends BaseSprayService { this: HttpService =>

  import marshalling._
  private lazy val sessionManager = WebGalleryActorSelection.sessionManagerSelection

  protected def auth(r: Auth): Result[AuthResponse] = processRequest(r)
  protected def vkAuth(r: VKAuth): Result[AuthResponse] = processRequest(r)

  val authRoute =
    pathPrefix("api") {
      pathPrefix("auth") {
        (pathEnd & post) {
          dynamic {
            handleWith {
              auth
            }
          }
        } ~
        pathPrefix("callback") {
          (path("vk") & parameter('code)) { code =>
            println(s"code is $code")
            dynamic {
              handleWith(code :: HNil) {
                vkAuth
              }
            }
          }
        }
      } ~
      path("logout") {
        cookie(Hardcoded.CookieName) { sessionId =>
          deleteCookie(Hardcoded.CookieName, Hardcoded.CookiePath) {
            sessionManager ! DeleteSession(sessionId.value.toInt)
            redirect("", StatusCodes.Found)
          }
        }
      }
    }
}
