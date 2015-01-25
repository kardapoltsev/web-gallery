package com.github.kardapoltsev.webgallery.http

import com.github.kardapoltsev.webgallery.StatsManager.GetStats
import com.github.kardapoltsev.webgallery.ValidationManager.ValidateLogin
import shapeless.HNil
import spray.routing.{ Route, HttpService }
import spray.http._

/**
 * Created by alexey on 6/4/14.
 */
trait ValidationSprayService extends BaseSprayService { this: HttpService =>
  import marshalling._

  protected def validateLogin(r: ValidateLogin) = processRequest(r)

  val validationLoginRoute: Route =
    path("api" / "validate" / "login" / Segment) { authId =>
      dynamic {
        handleRequest(authId :: HNil)(validateLogin)
      }
    }
}
