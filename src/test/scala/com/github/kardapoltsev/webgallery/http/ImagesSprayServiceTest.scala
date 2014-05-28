package com.github.kardapoltsev.webgallery.http


import org.scalatest.{Matchers, FlatSpec}
import spray.testkit.{ScalatestRouteTest, Specs2RouteTest}
import spray.routing.HttpService
import akka.actor.ActorRefFactory
import scala.concurrent.Future
import com.github.kardapoltsev.webgallery.Database.{GetTagsResponse, GetFilesResponse}
import akka.util.Timeout
import scala.concurrent.duration.FiniteDuration
import spray.http.StatusCodes



/**
 * Created by alexey on 5/28/14.
 */
class ImagesSprayServiceTest extends FlatSpec with Matchers with ScalatestRouteTest
  with HttpService with ImagesSprayService {
  override def actorRefFactory = system
  override implicit val executionContext = system.dispatcher
  override implicit val requestTimeout = Timeout(FiniteDuration(3, concurrent.duration.SECONDS))

  override protected def getTags: Future[GetTagsResponse] =
    Future.successful(GetTagsResponse(Seq.empty))

  override protected def getByAlbum(album: String): Future[GetFilesResponse] =
    Future.successful(GetFilesResponse(Seq.empty))


  "ImagesSprayService" should "respond to `/'" in {
    Get() ~> imagesRoute ~> check {
      status should be(StatusCodes.OK)
    }
  }
  it should "respond to /albums/album1" in {
    Get("/albums/album1") ~> imagesRoute ~> check {
      status should be(StatusCodes.OK)
    }
  }
  it should "respond to /assets/js/main.js" in {
    Get("/assets/js/main.js") ~> imagesRoute ~> check {
      status should be(StatusCodes.OK)
    }
  }
}
