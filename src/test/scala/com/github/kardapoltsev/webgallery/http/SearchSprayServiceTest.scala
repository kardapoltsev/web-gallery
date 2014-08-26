package com.github.kardapoltsev.webgallery.http


import com.github.kardapoltsev.webgallery.Database
import spray.routing.HttpService
import spray.testkit.ScalatestRouteTest
import org.scalatest.{FlatSpec, Matchers}
import akka.util.Timeout
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.Future
import spray.http.{ContentTypes, StatusCodes}
import com.github.kardapoltsev.webgallery.TagsManager.{SearchTags, GetTagsResponse}



/**
 * Created by alexey on 5/30/14.
 */
class SearchSprayServiceTest extends FlatSpec with Matchers with ScalatestRouteTest
  with HttpService with SearchSprayService {
  import com.github.kardapoltsev.webgallery.db._
  import marshalling._
  override def actorRefFactory = system
  override implicit val executionContext = system.dispatcher
  override implicit val requestTimeout = Timeout(FiniteDuration(3, concurrent.duration.SECONDS))

  private val getTagsResponse = GetTagsResponse(Seq.empty)
  override protected def searchTags(r: SearchTags) =
    Future.successful(Right(getTagsResponse))

  behavior of "SearchSprayService"

  it should "respond to /api/search/tags?query=test" in {
    Get("/api/search/tags?term=test") ~> searchRoute ~> check {
      responseAs[GetTagsResponse] should be(getTagsResponse)
      status should be(StatusCodes.OK)
      contentType should be(ContentTypes.`application/json`)
    }
  }
}
