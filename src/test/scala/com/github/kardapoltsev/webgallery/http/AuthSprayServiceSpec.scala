package com.github.kardapoltsev.webgallery.http

import akka.util.Timeout
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
class AuthSprayServiceSpec extends FlatSpec with Matchers with ScalatestRouteTest
  with HttpService with AuthSprayService {

  import com.github.kardapoltsev.webgallery.http.BaseSprayService._

  override def actorRefFactory = system
  override implicit val executionContext = system.dispatcher
  override implicit val requestTimeout = Timeout(FiniteDuration(3, concurrent.duration.SECONDS))
  override protected def auth(r: Auth): Result[AuthResponse] =
    Future.successful(Left(ErrorResponse.NotFound))

  behavior of "UsersSprayService"

  it should "handle auth request" in {
    Post("/api/auth", HttpEntity(
      ContentTypes.`application/json`,
      Auth("test", AuthType.Direct, "password").toJson.compactPrint)) ~> authRoute ~> check {
      status should be(StatusCodes.NotFound)
    }
  }

}
