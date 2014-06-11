package com.github.kardapoltsev.webgallery.http

import org.scalatest.{Matchers, FlatSpec}
import spray.testkit.ScalatestRouteTest
import spray.routing.HttpService
import scala.concurrent.Future
import com.github.kardapoltsev.webgallery.Database.{GetTags, CreateTagResponse, CreateTag, GetTagsResponse}
import akka.util.Timeout
import scala.concurrent.duration.FiniteDuration
import spray.http.{HttpEntity, FormData, StatusCodes}
import com.github.kardapoltsev.webgallery.http.marshalling._
import com.github.kardapoltsev.webgallery.Database


/**
 * Created by alexey on 5/28/14.
 */
class TagsSprayServiceTest extends FlatSpec with Matchers with ScalatestRouteTest
  with HttpService with TagsSprayService {
  import BaseSprayService._
  import spray.json._
  import com.github.kardapoltsev.webgallery.db._

  override def actorRefFactory = system
  override implicit val executionContext = system.dispatcher
  override implicit val requestTimeout = Timeout(FiniteDuration(3, concurrent.duration.SECONDS))

  protected def createTag(r: CreateTag): Result[CreateTagResponse] =
    Future.successful(Right(CreateTagResponse(Tag(0, ""))))

  override protected def getTags(r: GetTags.type): Result[GetTagsResponse] = {
    Future.successful(Right(GetTagsResponse(Seq.empty)))
  }


  "TagsSprayService" should "return all tags" in {
    Get("/api/tags") ~> tagsRoute ~> check {
      status should be(StatusCodes.OK)
    }
  }

  it should "create tags" in {
    Post("/api/tags", HttpEntity(Tag(0, "test").toJson.compactPrint)) ~> tagsRoute ~> check {
      status should be(StatusCodes.OK)
    }
  }
}
