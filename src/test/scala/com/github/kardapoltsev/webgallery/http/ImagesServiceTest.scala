package com.github.kardapoltsev.webgallery.http


import com.github.kardapoltsev.webgallery.ImageManager._
import com.github.kardapoltsev.webgallery.TestBase
import com.github.kardapoltsev.webgallery.util.Hardcoded
import spray.http._



/**
 * Created by alexey on 5/28/14.
 */
class ImagesServiceTest extends TestBase with ImagesSprayService {
  import marshalling._

  behavior of "ImagesService"

  it should "upload image and extract metadata" in {
    authorized { implicit auth =>
      val imageId = createImage
      val image = getImage(imageId)
      image.tags.map(_.name) should be(Seq("nikon d7000", "2014-05-10"))
    }
  }

  it should "patch image" in {
    authorized { implicit auth =>
      val imageId = createImage
      val tag = createTag()
      println(s"adding $tag for $imageId")
      addTag(imageId, tag)
      val image = getImage(imageId)
      println(s"got $image")
      image.tags.exists(_.name == tag.name) should be(true)
    }
  }

  it should "return image by id" in {
    authorized { implicit auth =>
      val imageId = createImage
      getImage(imageId).id should be(imageId)
    }
  }

  it should "show public images to anonymous" in {
    val imageId = authorized { implicit auth =>
      val imageId = createImage
      val tag = getImage(imageId).tags.head
      addGrantees(tag.id, Hardcoded.AnonymousUserId)
      imageId
    }
    Get(s"/api/images/$imageId") ~> imagesRoute ~> check {
      status should be(StatusCodes.OK)
    }
  }

  it should "show public images to other user" in {
    val imageId = authorized { implicit auth =>
      val imageId = createImage
      val tag = getImage(imageId).tags.head
      addGrantees(tag.id, Hardcoded.AnonymousUserId)
      imageId
    }
    authorizedRandomUser { implicit auth =>
      getImage(imageId).id should be(imageId)
    }
  }

  it should "not show private images to anonymous" in {
    val imageId = authorized { implicit auth =>
      createImage
    }
    Get(s"/api/images/$imageId") ~> imagesRoute ~> check {
      status should be(StatusCodes.Forbidden)
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
