package com.github.kardapoltsev.webgallery


import java.io.File

import akka.testkit.TestKitBase
import akka.util.Timeout
import com.github.kardapoltsev.webgallery.AclManager.GetGranteesResponse
import com.github.kardapoltsev.webgallery.Database.{UpdateImageParams, GetImageResponse}
import com.github.kardapoltsev.webgallery.ImageManager.UploadImageResponse
import com.github.kardapoltsev.webgallery.TagsManager.CreateTagResponse
import com.github.kardapoltsev.webgallery.UserManager.{GetUserResponse, AuthResponse, RegisterUser}
import com.github.kardapoltsev.webgallery.db._
import com.github.kardapoltsev.webgallery.db.gen.FakeDataCreator
import com.github.kardapoltsev.webgallery.dto.ImageInfo
import com.github.kardapoltsev.webgallery.http.{AclSprayService, TagsSprayService, ImagesSprayService, UserSprayService}
import com.github.kardapoltsev.webgallery.util.Hardcoded
import org.joda.time.{DateTimeZone, DateTime}
import org.scalatest.{BeforeAndAfterEach, BeforeAndAfterAll, FlatSpec, Matchers}
import spray.http._
import spray.routing.HttpService
import spray.testkit.{ScalatestRouteTest, RouteTest}



/**
 * Created by alexey on 6/24/14.
 */
trait TestBase extends FlatSpec with Matchers with UserSprayService with ImagesSprayService with TagsSprayService
  with AclSprayService with ScalatestRouteTest with HttpService with BeforeAndAfterEach {
  import com.github.kardapoltsev.webgallery.http.marshalling._
  import spray.json._
  protected val dsc2845 = new File(getClass.getResource("/DSC_2845.jpg").toURI)

  Server.init()
  override def actorRefFactory = system
  override implicit val executionContext = system.dispatcher
  override implicit val requestTimeout = Configs.Timeouts.LongRunning
  implicit val routeTestTimeout = RouteTestTimeout(Configs.Timeouts.Background.duration)

  override def afterEach(): Unit = {
    Database.cleanDatabase()
  }
  protected val login = "test"
  protected val emailDomain = "@example.com"
  protected val password = "password"

  protected def authorized[A](f: AuthResponse => A): A = {
    val auth =  registerUser(login)
    f(auth)
  }


  protected def randomUserId: UserId = registerUser().userId


  private def registerUser(username: String = java.util.UUID.randomUUID().toString): AuthResponse = {
    Post("/api/users", HttpEntity(
      ContentTypes.`application/json`,
      RegisterUser(username, username + emailDomain, AuthType.Direct, Some(password)).toJson.compactPrint)) ~> usersRoute ~> check {
      status should be(StatusCodes.Found)
      responseAs[AuthResponse]
    }
  }


  protected def withCookie(request: HttpRequest)(implicit auth: AuthResponse): HttpRequest = {
    val cookie = HttpHeaders.Cookie(HttpCookie(Hardcoded.CookieName, auth.sessionId.toString))
    request.withHeaders(cookie)
  }


  protected def getImage(imageId: ImageId)(implicit auth: AuthResponse): ImageInfo = {
    withCookie(Get(s"/api/images/$imageId")) ~> imagesRoute ~> check {
      status should be(StatusCodes.OK)
      responseAs[GetImageResponse].image
    }
  }


  protected def createImage(implicit auth: AuthResponse): ImageId = {
    val request = withCookie(Post("/api/upload", MultipartFormData(Seq(BodyPart(dsc2845, "file")))))
    request ~> imagesRoute ~> check {
      status should be(StatusCodes.OK)
      responseAs[UploadImageResponse].imageId
    }
  }


  protected def createTag(name: String = "test")(implicit auth: AuthResponse): Tag = {
    val request = withCookie(
      Post(s"/api/users/${auth.userId}/tags",
        HttpEntity(Tag(0, 0, name, DateTime.now(DateTimeZone.UTC)).toJson.compactPrint))
      )
    request ~> tagsRoute ~> check {
      status should be(StatusCodes.OK)
      responseAs[CreateTagResponse].tag
    }
  }


  protected def addTag(imageId: ImageId, tag: Tag)(implicit auth: AuthResponse): Unit = {
    val image = getImage(imageId)
    val tags = tag +: image.tags
    withCookie(Patch( s"/api/images/$imageId",
      HttpEntity(ContentTypes.`application/json`,
        UpdateImageParams(Some(tags)).toJson.compactPrint))) ~> imagesRoute ~> check {
      status should be(StatusCodes.OK)
    }
  }


  protected def getUser(userId: UserId)(implicit auth: AuthResponse): User = {
    withCookie(Get(s"/api/users/$userId")) ~> usersRoute ~> check {
      status should be(StatusCodes.OK)
      responseAs[GetUserResponse].user
    }
  }
}
