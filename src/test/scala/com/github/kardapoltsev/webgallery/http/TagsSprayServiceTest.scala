package com.github.kardapoltsev.webgallery.http

import org.scalatest.{Matchers, FlatSpec}
import spray.testkit.ScalatestRouteTest
import spray.routing.HttpService
import scala.concurrent.Future
import com.github.kardapoltsev.webgallery.Database.{CreateTag, GetTagsResponse}
import akka.util.Timeout
import scala.concurrent.duration.FiniteDuration
import spray.http.{HttpEntity, FormData, StatusCodes}
import com.github.kardapoltsev.webgallery.db.{Tag, Image}



/**
 * Created by alexey on 5/28/14.
 */
class TagsSprayServiceTest extends FlatSpec with Matchers with ScalatestRouteTest
  with HttpService with TagsSprayService {

  import spray.json._

  override def actorRefFactory = system
  override implicit val executionContext = system.dispatcher
  override implicit val requestTimeout = Timeout(FiniteDuration(3, concurrent.duration.SECONDS))

  override protected def getTags: Future[Seq[Tag]] = Future.successful(Seq.empty)

  override protected def createTag(request: CreateTag): Future[Tag] = Future.successful(Tag(request.name, 10))


  "TagsSprayService" should "return all tags" in {
    Get("/api/tags") ~> tagsRoute ~> check {
      status should be(StatusCodes.OK)
    }
  }

  it should "create tags" in {
    Post("/api/tags", HttpEntity(Tag("test").toJson.compactPrint)) ~> tagsRoute ~> check {
      status should be(StatusCodes.OK)
    }
  }
}
