package com.github.kardapoltsev.webgallery.http


import com.github.kardapoltsev.webgallery.AclManager.{GetGranteesResponse, GetGrantees, RevokeAccess, GrantAccess}
import com.github.kardapoltsev.webgallery.{TestBase, Server, Database}
import com.github.kardapoltsev.webgallery.http.BaseSprayService.Result
import spray.http.{HttpEntity, ContentTypes, StatusCodes}
import spray.json.{JsNumber, JsArray}

import scala.concurrent.Future



/**
 * Created by alexey on 5/30/14.
 */
class AclSprayServiceSpec extends TestBase with AclSprayService {


  import marshalling._


  private val getGranteesResponse = GetGranteesResponse(Seq.empty)
  override protected def grantAccess(r: GrantAccess): Result[SuccessResponse] =
    Future.successful(Right(SuccessResponse))
  override protected def revokeAccess(r: RevokeAccess): Result[SuccessResponse] =
    Future.successful(Right(SuccessResponse))
//  override protected def getGrantees(r: GetGrantees): Result[GetGranteesResponse] =
//    Future.successful(Right(getGranteesResponse))

  behavior of "AclSprayService"

  //TODO: fix get tag test
  it should "respond to GET /api/acl/tag/{tagId}" in {
    authorized { implicit auth =>
      withCookie(Get(s"/api/acl/tag/${auth.userId}")) ~> aclRoute ~> check {
        status should be(StatusCodes.Forbidden)
//        contentType should be(ContentTypes.`application/json`)
      }
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
