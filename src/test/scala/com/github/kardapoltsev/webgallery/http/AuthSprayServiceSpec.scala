package com.github.kardapoltsev.webgallery.http

import com.github.kardapoltsev.webgallery.TestBase
import com.github.kardapoltsev.webgallery.UserManager._
import com.github.kardapoltsev.webgallery.db.AuthType
import spray.http.{ ContentTypes, HttpEntity, StatusCodes }

/**
 * Created by alexey on 6/18/14.
 */
class AuthSprayServiceSpec extends TestBase with AuthSprayService {

  import marshalling._

  behavior of "AuthSprayService"

  it should "handle auth request" in {
    authorized { auth => } //register user

    Post("/api/auth", HttpEntity(
      ContentTypes.`application/json`,
      Auth(login + emailDomain, AuthType.Direct, password).toJson.compactPrint)) ~> authRoute ~> check {
      status should be(StatusCodes.Found)
      responseAs[AuthResponse]
    }
  }

  it should "handle logout request" in {
    authorized { implicit auth =>
      val request = withCookie(Get("/api/logout"))
      request ~> authRoute ~> check {
        status should be(StatusCodes.Found)
      }

      withCookie(Get(s"/api/users/current")) ~> usersRoute ~> check {
        status should be(StatusCodes.Unauthorized)
      }
    }
  }

  it should "not authorize  non existing user" in {
    Post("/api/auth", HttpEntity(
      ContentTypes.`application/json`,
      Auth("non existing login" + emailDomain, AuthType.Direct, password).toJson.compactPrint)) ~> authRoute ~> check {
      status should be(StatusCodes.NotFound)

    }

  }

  it should "not authorize user with wrong password" in {
    Post("/api/auth", HttpEntity(
      ContentTypes.`application/json`,
      Auth(login + emailDomain, AuthType.Direct, "non existing password").toJson.compactPrint)) ~> authRoute ~> check {
      status should be(StatusCodes.NotFound)

    }

  }

}
