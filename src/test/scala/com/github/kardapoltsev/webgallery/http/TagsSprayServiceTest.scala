package com.github.kardapoltsev.webgallery.http


import org.joda.time.{DateTimeZone, DateTime}
import org.scalatest.{Matchers, FlatSpec}
import spray.testkit.ScalatestRouteTest
import spray.routing.HttpService
import scala.concurrent.Future
import com.github.kardapoltsev.webgallery.TagsManager._
import akka.util.Timeout
import scala.concurrent.duration.FiniteDuration
import spray.http.{HttpEntity, StatusCodes}


/**
 * Created by alexey on 5/28/14.
 */
class TagsSprayServiceTest extends FlatSpec with Matchers with ScalatestRouteTest
  with HttpService with TagsSprayService {
  import BaseSprayService._
  import spray.json._
  import com.github.kardapoltsev.webgallery.db._
  import marshalling._

  override def actorRefFactory = system
  override implicit val executionContext = system.dispatcher
  override implicit val requestTimeout = Timeout(FiniteDuration(3, concurrent.duration.SECONDS))

  override protected def createTag(r: CreateTag): Result[CreateTagResponse] =
    Future.successful(Right(CreateTagResponse(Tag(0, 0, "createdTag", DateTime.now()))))

  val getTagsResponse = GetTagsResponse(Seq.empty)
  override protected def getTags(r: GetTags): Result[GetTagsResponse] = {
    Future.successful(Right(getTagsResponse))
  }

  val getRecentTagsResponse = GetTagsResponse(Seq(Tag(0, 0, "testTag", DateTime.now(DateTimeZone.UTC))))
  override protected def getRecentTags(r: GetRecentTags): Result[GetTagsResponse] = {
    Future.successful(Right(getRecentTagsResponse))
  }


  "TagsSprayService" should "return user tags" in {
    Get("/api/users/1/tags") ~> tagsRoute ~> check {
      status should be(StatusCodes.OK)
      responseAs[GetTagsResponse] should be(getTagsResponse)
    }
  }

  it should "return recent tags" in {
    Get("/api/users/1/tags/recent") ~> tagsRoute ~> check {
      status should be(StatusCodes.OK)
      responseAs[GetTagsResponse] should be(getRecentTagsResponse)
    }
  }

  it should "create tags" in {
    Post("/api/users/1/tags", HttpEntity(Tag(0, 0, "test", DateTime.now()).toJson.compactPrint)) ~> tagsRoute ~> check {
      status should be(StatusCodes.OK)
    }
  }
}
