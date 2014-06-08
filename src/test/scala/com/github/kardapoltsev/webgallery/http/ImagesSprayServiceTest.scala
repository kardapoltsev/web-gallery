package com.github.kardapoltsev.webgallery.http


import org.scalatest.{Matchers, FlatSpec}
import spray.testkit.{ScalatestRouteTest, Specs2RouteTest}
import spray.routing.HttpService
import akka.actor.ActorRefFactory
import scala.concurrent.Future
import com.github.kardapoltsev.webgallery.Database._
import akka.util.Timeout
import scala.concurrent.duration.FiniteDuration
import spray.http.{FormData, HttpEntity, StatusCodes}
import com.github.kardapoltsev.webgallery.db.{Alternative, Image}
import com.github.kardapoltsev.webgallery.Database.UpdateImage
import com.github.kardapoltsev.webgallery.ImageProcessor.TransformImageRequest


/**
 * Created by alexey on 5/28/14.
 */
class ImagesSprayServiceTest extends FlatSpec with Matchers with ScalatestRouteTest
  with HttpService with ImagesSprayService {
  import marshalling._
  override def actorRefFactory = system
  override implicit val executionContext = system.dispatcher
  override implicit val requestTimeout = Timeout(FiniteDuration(3, concurrent.duration.SECONDS))

  override protected def getImage(imageId: Int): Future[Option[Image]] = Future.successful(None)
  override protected def updateImage(request: UpdateImage): Future[InternalResponse] =
    Future.successful(SuccessResponse)
  override protected def getByTag(tagName: String): Future[Seq[Image]] = Future.successful(Seq.empty)
  protected def transformImage(request: TransformImageRequest): Future[Alternative] =
    Future.successful(Alternative(request.imageId, "", request.transform))


  it should "patch image" in {
    Patch("/api/images/5", UpdateImageParams(None).toJson.compactPrint) ~> imagesRoute ~> check {
      status should be(StatusCodes.OK)
    }
  }
  it should "return image by id" in {
    Get("/api/images/5") ~> imagesRoute ~> check {
      status should be(StatusCodes.NotFound)
    }
  }
  it should "return images with tag" in {
    Get("/api/images?tag=test") ~> imagesRoute ~> check {
      status should be(StatusCodes.OK)
    }
  }
}
