package com.github.kardapoltsev.webgallery.http


import com.github.kardapoltsev.webgallery.TestBase
import com.github.kardapoltsev.webgallery.Database._
import spray.http._



/**
 * Created by alexey on 5/28/14.
 */
class ImagesSprayServiceTest extends TestBase with ImagesSprayService {
  import marshalling._

  import concurrent.duration._
  implicit val routeTestTimeout = RouteTestTimeout(10.seconds)

  behavior of "ImagesService"

  it should "patch image" in {
    authorized { implicit auth =>
      val imageId = createImage
      val tag = createTag
      addTag(imageId, tag)
      val image = getImage(imageId)
      println(s"adding $tag for $imageId")
      println(s"got $image")
      image.tags.contains(tag) should be(true)
    }
  }
  it should "return image by id" in {
    authorized { implicit auth =>
      val imageId = createImage
      getImage(imageId).id should be(imageId)
    }
  }
  it should "return images with tag" in {
    authorized { implicit auth =>
      val imageId = createImage
      val image = getImage(imageId)
      val tag = image.tags.head

      withCookie(Get(s"/api/images?tagId=${tag.id}")) ~> imagesRoute ~> check {
        status should be(StatusCodes.OK)
        val images = responseAs[GetImagesResponse].images
        images.exists {
          i => i.tags.contains(tag)
        } should be(true)
      }
    }
  }
  it should "transform image by width and height" in {
    authorized { implicit auth =>
      val imageId = createImage
      val request = withCookie(Get(s"/api/images/$imageId/file?width=100&height=100&scaleType=FitSource"))
      request ~> imagesRoute ~> check {
        status should be(StatusCodes.OK)
      }
    }
  }
  it should "transform image by width" in {
    authorized { implicit auth =>
      val imageId = createImage
      val request = withCookie(Get(s"/api/images/$imageId/file?width=100&scaleType=FitSource"))
      request ~> imagesRoute ~> check {
        status should be(StatusCodes.OK)
      }
    }
  }
}
