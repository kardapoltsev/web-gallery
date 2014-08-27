package com.github.kardapoltsev.webgallery.http


import com.github.kardapoltsev.webgallery.db.gen.User
import org.joda.time.{DateTimeZone, DateTime}
import spray.routing.HttpService
import spray.testkit.ScalatestRouteTest
import org.scalatest.{FlatSpec, Matchers}
import com.github.kardapoltsev.webgallery.UserManager._
import scala.concurrent.Future
import spray.http.{StatusCodes, HttpEntity, ContentTypes}
import com.github.kardapoltsev.webgallery.db.AuthType
import akka.util.Timeout
import scala.concurrent.duration.FiniteDuration



/**
 * Created by alexey on 6/18/14.
 */
class UserSprayServiceSpec extends FlatSpec with Matchers with ScalatestRouteTest
  with HttpService with UserSprayService {

  import BaseSprayService._
  import marshalling._

  override def actorRefFactory = system
  override implicit val executionContext = system.dispatcher
  override implicit val requestTimeout = Timeout(FiniteDuration(3, concurrent.duration.SECONDS))

  override protected def registerUser(r: RegisterUser): Result[AuthResponse] =
    Future.successful(Left(ErrorResponse.Conflict))

  override protected def getUser(r: GetUser): Result[GetUserResponse] =
    Future.successful(Left(ErrorResponse.NotFound))

  val getCurrentUserResponse = GetUserResponse(User(0, "currentUser", 1, DateTime.now(DateTimeZone.UTC)))
  override protected def getCurrentUser(r: GetCurrentUser): Result[GetUserResponse] =
    Future.successful(Right(getCurrentUserResponse))

  behavior of "UsersSprayService"

  it should "handle register user request" in {
    Post("/api/users", HttpEntity(
      ContentTypes.`application/json`,
      RegisterUser("test", "test", AuthType.Direct, Some("password")).toJson.compactPrint)) ~> usersRoute ~> check {
      status should be(StatusCodes.Conflict)
    }
  }

  it should "handle get user request" in {
    Get("/api/users/123") ~> usersRoute ~> check {
      status should be(StatusCodes.NotFound)
    }
  }

  it should "handle get current user request" in {
    Get("/api/users/current") ~> usersRoute ~> check {
      status should be(StatusCodes.OK)
      responseAs[GetUserResponse] should be(getCurrentUserResponse)
    }
  }

}
