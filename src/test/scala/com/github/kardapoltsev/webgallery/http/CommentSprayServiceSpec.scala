package com.github.kardapoltsev.webgallery.http

import akka.util.Timeout
import com.github.kardapoltsev.webgallery.CommentManager.{GetCommentsResponse, GetComments, AddCommentResponse, AddComment}
import com.github.kardapoltsev.webgallery.db._
import com.github.kardapoltsev.webgallery.http.BaseSprayService.Result
import org.joda.time.DateTime
import org.scalatest.{FlatSpec, Matchers}
import spray.http.{ContentTypes, HttpEntity, StatusCodes}
import spray.routing.HttpService
import spray.testkit.ScalatestRouteTest

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration



/**
 * Created by alexey on 5/30/14.
 */
class CommentSprayServiceSpec extends FlatSpec with Matchers with ScalatestRouteTest
  with HttpService with CommentSprayService {

  import marshalling._

  override def actorRefFactory = system
  override implicit val executionContext = system.dispatcher
  override implicit val requestTimeout = Timeout(FiniteDuration(3, concurrent.duration.SECONDS))


  val addCommentResponse = AddCommentResponse(Comment(1, 1, None, "", DateTime.now(), 1))
  override protected def addComment(r: AddComment): Result[AddCommentResponse] =
    Future.successful(Right(addCommentResponse))
  val getCommentsResponse = GetCommentsResponse(Seq.empty)
  override def getComments(r: GetComments) = Future.successful(Right(getCommentsResponse))

  behavior of "SearchSprayService"

  it should "respond to POST /api/images/1/comments" in {
    Post("/api/images/1/comments",
      HttpEntity(ContentTypes.`application/json`, AddCommentBody("", None).toJson.compactPrint)
    ) ~> commentRoute ~> check {
      status should be(StatusCodes.OK)
      contentType should be(ContentTypes.`application/json`)
    }
  }

  it should "respond to GET /api/images/1/comments" in {
    Get("/api/images/1/comments?limit=3") ~> commentRoute ~> check {
      status should be(StatusCodes.OK)
      responseAs[GetCommentsResponse] should be(getCommentsResponse)
    }
  }
}
