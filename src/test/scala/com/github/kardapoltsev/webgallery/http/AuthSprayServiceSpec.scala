package com.github.kardapoltsev.webgallery.http

import com.github.kardapoltsev.webgallery.TestBase
import com.github.kardapoltsev.webgallery.UserManager._
import com.github.kardapoltsev.webgallery.db.AuthType
import spray.http.{ContentTypes, HttpEntity, StatusCodes}



/**
 * Created by alexey on 6/18/14.
 */
class AuthSprayServiceSpec extends TestBase with AuthSprayService {

  import marshalling._


  behavior of "AuthSprayService"

  it should "handle auth request" in {
    authorized{auth =>} //register user

    Post("/api/auth", HttpEntity(
      ContentTypes.`application/json`,
      Auth(login + emailDomain, AuthType.Direct, password).toJson.compactPrint)) ~> authRoute ~> check {
      status should be(StatusCodes.Found)
      responseAs[AuthResponse]
    }
  }

}
