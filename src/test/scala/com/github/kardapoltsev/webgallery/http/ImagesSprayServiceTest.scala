package com.github.kardapoltsev.webgallery.http


import com.github.kardapoltsev.webgallery.http.BaseSprayService.Result
import org.scalatest.{Matchers, FlatSpec}
import spray.testkit.{ScalatestRouteTest, Specs2RouteTest}
import spray.routing.HttpService
import akka.actor.ActorRefFactory
import scala.concurrent.Future
import com.github.kardapoltsev.webgallery.Database._
import akka.util.Timeout
import scala.concurrent.duration.FiniteDuration
import spray.http._
import com.github.kardapoltsev.webgallery.Database.UpdateImage
import com.github.kardapoltsev.webgallery.ImageProcessor.{TransformImageResponse, TransformImageRequest}
import com.github.kardapoltsev.webgallery.db._
import com.github.kardapoltsev.webgallery.db.gen
import com.github.kardapoltsev.webgallery.dto.ImageInfo
import com.github.kardapoltsev.webgallery.Database.UpdateImage
import com.github.kardapoltsev.webgallery.Database.UpdateImageParams



/**
 * Created by alexey on 5/28/14.
 */
class ImagesSprayServiceTest extends FlatSpec with Matchers with ScalatestRouteTest
  with HttpService with ImagesSprayService {
  import marshalling._
  override def actorRefFactory = system
  override implicit val executionContext = system.dispatcher
  override implicit val requestTimeout = Timeout(FiniteDuration(3, concurrent.duration.SECONDS))

  override protected def getImage(r: GetImage) = Future.successful(Left(ErrorResponse.NotFound))
  override protected def updateImage(request: UpdateImage) = Future.successful(Right(SuccessResponse))
  override protected def getByTag(r: GetByTag) = Future.successful(Right(GetImagesResponse(Seq.empty)))
  override protected def transformImage(request: TransformImageRequest): Result[TransformImageResponse] =
    Future.successful(Right(TransformImageResponse(gen.Alternative(
      0, request.imageId, "", request.size.optWidth.getOrElse(10),
      request.size.optHeight.getOrElse(10),
      request.size.scaleType.toString
    ))))


  it should "patch image" in {
    Patch("/api/images/5", HttpEntity(ContentTypes.`application/json`, UpdateImageParams(None).toJson.compactPrint)) ~> imagesRoute ~> check {
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
  it should "transform image by width and height" in {
    Get("/api/images/5/file?width=100&height=100&scaleType=FitSource") ~> imagesRoute ~> check {
      status should be(StatusCodes.OK)
    }
  }
  it should "transform image by width" in {
    Get("/api/images/5/file?width=100&scaleType=FitSource") ~> imagesRoute ~> check {
      status should be(StatusCodes.OK)
    }
  }
}
