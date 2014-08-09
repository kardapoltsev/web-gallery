package com.github.kardapoltsev.webgallery.http


import com.github.kardapoltsev.webgallery.AclManager.{GetGranteesResponse, GetGrantees, RevokeAccess, GrantAccess}
import spray.routing.{Route, HttpService}
import scala.concurrent.{Future, ExecutionContext}
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

  protected def grantAccess(r: GrantAccess): Result[SuccessResponse] = processRequest(r)
  protected def revokeAccess(r: RevokeAccess): Result[SuccessResponse] = processRequest(r)
  protected def getGrantees(r: GetGrantees): Result[GetGranteesResponse] = processRequest(r)


  val aclRoute: Route =
    pathPrefix("api" / "acl" / "tag") {
      path(IntNumber) { tagId =>
        put {
          dynamic {
            handleWith(tagId :: HNil) {
              grantAccess
            }
          }
        } ~
        delete {
          dynamic {
            handleWith(tagId :: HNil) {
              revokeAccess
            }
          }
        } ~
        get {
          dynamic {
            handleWith(tagId :: HNil) {
              getGrantees
            }
          }
        }
      }
    }
}
