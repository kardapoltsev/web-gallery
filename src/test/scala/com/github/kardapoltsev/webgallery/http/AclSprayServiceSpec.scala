package com.github.kardapoltsev.webgallery.http

import akka.util.Timeout
import com.github.kardapoltsev.webgallery.AclManager.{GetGranteesResponse, GetGrantees, RevokeAccess, GrantAccess}
import com.github.kardapoltsev.webgallery.http.BaseSprayService.Result
import org.scalatest.{FlatSpec, Matchers}
import spray.http.{HttpEntity, ContentTypes, StatusCodes}
import spray.json.{JsNumber, JsArray}
import spray.routing.HttpService
import spray.testkit.ScalatestRouteTest

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration



/**
 * Created by alexey on 5/30/14.
 */
class AclSprayServiceSpec extends FlatSpec with Matchers with ScalatestRouteTest
  with HttpService with AclSprayService {
  import marshalling._
  override def actorRefFactory = system
  override implicit val executionContext = system.dispatcher
  override implicit val requestTimeout = Timeout(FiniteDuration(3, concurrent.duration.SECONDS))

  private val getGranteesResponse = GetGranteesResponse(Seq.empty)
  override protected def grantAccess(r: GrantAccess): Result[SuccessResponse] =
    Future.successful(Right(SuccessResponse))
  override protected def revokeAccess(r: RevokeAccess): Result[SuccessResponse] =
    Future.successful(Right(SuccessResponse))
  override protected def getGrantees(r: GetGrantees): Result[GetGranteesResponse] =
    Future.successful(Right(getGranteesResponse))

  behavior of "AclSprayService"

  it should "respond to GET /api/acl/tag/1" in {
    Get("/api/acl/tag/1") ~> aclRoute ~> check {
      responseAs[GetGranteesResponse] should be(getGranteesResponse)
      status should be(StatusCodes.OK)
      contentType should be(ContentTypes.`application/json`)
    }
  }

  it should "respond to PUT /api/acl/tag/1" in {
    Put("/api/acl/tag/1",
      HttpEntity(ContentTypes.`application/json`, JsArray(JsNumber(1)).compactPrint)) ~> aclRoute ~> check {
        status should be(StatusCodes.OK)
      }
  }

  it should "respond to DELETE /api/acl/tag/1" in {
    Delete("/api/acl/tag/1",
      HttpEntity(ContentTypes.`application/json`, JsArray(JsNumber(1)).compactPrint)) ~> aclRoute ~> check {
      status should be(StatusCodes.OK)
    }
  }
}
