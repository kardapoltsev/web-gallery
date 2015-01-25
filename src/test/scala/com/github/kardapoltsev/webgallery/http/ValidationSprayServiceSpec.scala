package com.github.kardapoltsev.webgallery.http

import com.github.kardapoltsev.webgallery.TestBase
import com.github.kardapoltsev.webgallery.UserManager._
import com.github.kardapoltsev.webgallery.ValidationManager.ValidateLoginResponse
import com.github.kardapoltsev.webgallery.db.AuthType
import spray.http.{ ContentTypes, HttpEntity, StatusCodes }

/**
 * Created by alexey on 6/18/14.
 */
class ValidationSprayServiceSpec extends TestBase with ValidationSprayService {

  import marshalling._
  behavior of "ValidationSprayService"

  it should "validate existing login" in {

    val username = login
    registerUser(username)

    Get(s"/api/validate/login/${username + emailDomain}") ~> validationLoginRoute ~> check {
      status should be(StatusCodes.OK)
      contentType should be(ContentTypes.`application/json`)
      responseAs[ValidateLoginResponse].isValid should be(false)

    }
  }

  it should "validate non exsisting login" in {
    Get(s"/api/validate/login/test1111") ~> validationLoginRoute ~> check {
      status should be(StatusCodes.OK)
      contentType should be(ContentTypes.`application/json`)
      responseAs[ValidateLoginResponse].isValid should be(true)

    }
  }

}
