package com.github.kardapoltsev.webgallery.http


import com.github.kardapoltsev.webgallery.UserManager.{VKAuth, AuthResponse, Auth}
import com.github.kardapoltsev.webgallery.http.BaseSprayService.Result
import shapeless.HNil
import spray.routing.HttpService



/**
 * Created by alexey on 8/8/14.
 */
trait AuthSprayService extends BaseSprayService { this: HttpService =>

  import marshalling._

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
      }
    }
}
