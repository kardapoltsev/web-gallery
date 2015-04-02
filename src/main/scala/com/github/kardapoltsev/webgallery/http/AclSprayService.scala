package com.github.kardapoltsev.webgallery.http

import com.github.kardapoltsev.webgallery.SessionManager.DeleteSession
import com.github.kardapoltsev.webgallery.acl.AclManager
import AclManager.{ GetGranteesResponse, GetGrantees, RevokeAccess, GrantAccess }
import com.github.kardapoltsev.webgallery.util.Hardcoded
import spray.http.{ Uri, StatusCodes }
import spray.routing.{ Route, HttpService }
import scala.concurrent.{ Future, ExecutionContext }
import akka.util.Timeout
import shapeless._

/**
 * Created by alexey on 6/4/14.
 */
trait AclSprayService extends BaseSprayService { this: HttpService =>
  import marshalling._
  import spray.httpx.marshalling._
  import BaseSprayService._

  implicit def executionContext: ExecutionContext
  implicit def requestTimeout: Timeout

  protected def grantAccess(r: GrantAccess) = processRequest(r)
  protected def revokeAccess(r: RevokeAccess) = processRequest(r)
  protected def getGrantees(r: GetGrantees) = processRequest(r)

  val aclRoute: Route =
    pathPrefix("api" / "acl" / "tag") {
      path(IntNumber) { tagId =>
        put {
          perRequest(tagId :: HNil) {
            r: GrantAccess => HandlerWrapper[SuccessResponse](r)
          }
        } ~
          delete {
            perRequest(tagId :: HNil) {
              r: RevokeAccess => HandlerWrapper[SuccessResponse](r)
            }
          } ~
          get {
            perRequest(tagId :: HNil) {
              r: GetGrantees => HandlerWrapper[GetGranteesResponse](r)
            }
          }
      }
    } ~ path("logout") {
      deleteCookie(Hardcoded.CookieName) {
        cookie(Hardcoded.CookieName) { cookie =>
          router ! DeleteSession(cookie.value)
          redirect(Uri("/"), StatusCodes.Found)
        }
      }
    }
}
