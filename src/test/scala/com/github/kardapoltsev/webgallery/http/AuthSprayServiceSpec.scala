package com.github.kardapoltsev.webgallery.http

import akka.util.Timeout
import com.github.kardapoltsev.webgallery.TestBase
import com.github.kardapoltsev.webgallery.UserManager._
import com.github.kardapoltsev.webgallery.db.AuthType
import org.scalatest.{FlatSpec, Matchers}
import spray.http.{ContentTypes, HttpEntity, StatusCodes}
import spray.routing.HttpService
import spray.testkit.ScalatestRouteTest

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration



/**
 * Created by alexey on 6/18/14.
 */
class AuthSprayServiceSpec extends TestBase with AuthSprayService {

  import com.github.kardapoltsev.webgallery.http.BaseSprayService._
  import marshalling._


  behavior of "AuthSprayService"

  it should "handle auth request" in {
    authorized{auth =>} //register user

    Post("/api/auth", HttpEntity(
      ContentTypes.`application/json`,
      Auth(login, AuthType.Direct, password).toJson.compactPrint)) ~> authRoute ~> check {
      status should be(StatusCodes.Found)
      responseAs[AuthResponse]
    }
  }

}
