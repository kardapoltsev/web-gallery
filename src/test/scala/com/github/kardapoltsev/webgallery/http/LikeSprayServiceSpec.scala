package com.github.kardapoltsev.webgallery.http

import akka.util.Timeout
import com.github.kardapoltsev.webgallery.CommentManager.AddCommentResponse
import com.github.kardapoltsev.webgallery.LikeManager.{UnlikeImage, LikeImage}
import com.github.kardapoltsev.webgallery.http.BaseSprayService.Result
import org.scalatest.{FlatSpec, Matchers}
import spray.http.{ContentTypes, StatusCodes}
import spray.routing.HttpService
import spray.testkit.ScalatestRouteTest

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration



/**
 * Created by alexey on 5/30/14.
 */
class LikeSprayServiceSpec extends FlatSpec with Matchers with ScalatestRouteTest
  with HttpService with LikeSprayService {

  override def actorRefFactory = system
  override implicit val executionContext = system.dispatcher
  override implicit val requestTimeout = Timeout(FiniteDuration(3, concurrent.duration.SECONDS))


  override protected def like(r: LikeImage): Result[SuccessResponse] =
    Future.successful(Right(SuccessResponse))
  override protected def unlike(r: UnlikeImage): Result[SuccessResponse] =
    Future.successful(Right(SuccessResponse))

  behavior of "LikeSprayService"

  it should "respond to POST /api/images/1/likes" in {
    Post("/api/images/1/likes") ~> likeRoute ~> check {
      status should be(StatusCodes.OK)
    }
  }

  it should "respond to DELETE /api/images/1/likes" in {
    Delete("/api/images/1/likes") ~> likeRoute ~> check {
      status should be(StatusCodes.OK)
    }
  }
}
