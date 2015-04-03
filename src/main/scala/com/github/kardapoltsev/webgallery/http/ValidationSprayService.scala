package com.github.kardapoltsev.webgallery.http

import com.github.kardapoltsev.webgallery.StatsManager.GetStats
import com.github.kardapoltsev.webgallery.ValidationManager.{ ValidateLoginResponse, ValidateLogin }
import shapeless.HNil
import spray.routing.{ Route, HttpService }
import spray.http._

/**
 * Created by alexey on 6/4/14.
 */
trait ValidationSprayService extends BaseSprayService { this: HttpService =>
  import marshalling._

  val validationLoginRoute: Route =
    path("api" / "validate" / "login" / Segment) { authId =>
      perRequest(authId :: HNil) {
        createWrapper[ValidateLogin, ValidateLoginResponse]
      }
    }

}
