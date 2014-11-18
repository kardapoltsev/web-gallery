package com.github.kardapoltsev.webgallery.http

import com.github.kardapoltsev.webgallery.TestBase
import com.github.kardapoltsev.webgallery.UserManager._
import spray.http.{ StatusCodes, HttpEntity, ContentTypes }
import com.github.kardapoltsev.webgallery.db.{ User, UserId, AuthType }

/**
 * Created by alexey on 6/18/14.
 */
class UserSprayServiceSpec extends TestBase with UserSprayService {

  import BaseSprayService._
  import marshalling._

  behavior of "UsersSprayService"

  it should "handle register user request" in {
    Post("/api/users", HttpEntity(
      ContentTypes.`application/json`,
      RegisterUser(login, login, AuthType.Direct, Some("password")).toJson.compactPrint)) ~> usersRoute ~> check {
      status should be(StatusCodes.Found)
      responseAs[AuthResponse]
    }
  }

  it should "handle get user request" in {
    authorized { implicit auth =>
      getUser(auth.userId).id should be(auth.userId)
    }
  }

  it should "handle get current user request" in {
    authorized { implicit auth =>
      withCookie(Get(s"/api/users/current")) ~> usersRoute ~> check {
        status should be(StatusCodes.OK)
        responseAs[GetUserResponse]
      }
    }
  }

}
