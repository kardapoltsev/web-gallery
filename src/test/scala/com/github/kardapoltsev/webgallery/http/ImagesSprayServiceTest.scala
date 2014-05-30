package com.github.kardapoltsev.webgallery.http


import org.scalatest.{Matchers, FlatSpec}
import spray.testkit.{ScalatestRouteTest, Specs2RouteTest}
import spray.routing.HttpService
import akka.actor.ActorRefFactory
import scala.concurrent.Future
import com.github.kardapoltsev.webgallery.Database.{GetTagsResponse, GetFilesResponse}
import akka.util.Timeout
import scala.concurrent.duration.FiniteDuration
import spray.http.{FormData, HttpEntity, StatusCodes}
import com.github.kardapoltsev.webgallery.db.Image



/**
 * Created by alexey on 5/28/14.
 */
class ImagesSprayServiceTest extends FlatSpec with Matchers with ScalatestRouteTest
  with HttpService with ImagesSprayService {
  override def actorRefFactory = system
  override implicit val executionContext = system.dispatcher
  override implicit val requestTimeout = Timeout(FiniteDuration(3, concurrent.duration.SECONDS))

  override protected def getTags = Future.successful(Seq.empty)

  override protected def getByAlbum(album: String) = Future.successful(Seq.empty)

  override protected def getImage(imageId: Int): Future[Option[Image]] = Future.successful(None)

  override protected def addTags(imageId: Int, tags: Seq[String]): Unit = {}


  "ImagesSprayService" should "respond to `/'" in {
    Get() ~> imagesRoute ~> check {
      status should be(StatusCodes.OK)
    }
  }
  it should "respond to /albums/album1" in {
    Get("/tags/tags1") ~> imagesRoute ~> check {
      status should be(StatusCodes.OK)
    }
  }
  it should "respond to /assets/js/main.js" in {
    Get("/assets/js/main.js") ~> imagesRoute ~> check {
      status should be(StatusCodes.OK)
    }
  }
  it should "patch image" in {
     val urlEncodedForm = FormData(Map("tag" -> "test"))
    Patch("/images/5", urlEncodedForm) ~> imagesRoute ~> check {
      status should be(StatusCodes.OK)
    }
  }
  it should "show single image" in {
    Get("/images/5") ~> imagesRoute ~> check {
      status should be(StatusCodes.NotFound)
    }
  }
}
