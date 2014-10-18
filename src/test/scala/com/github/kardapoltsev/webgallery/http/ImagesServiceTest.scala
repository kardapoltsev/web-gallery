package com.github.kardapoltsev.webgallery.http


import com.github.kardapoltsev.webgallery.ImageManager._
import com.github.kardapoltsev.webgallery.TestBase
import com.github.kardapoltsev.webgallery.UserManager.AuthResponse
import com.github.kardapoltsev.webgallery.db.Image
import com.github.kardapoltsev.webgallery.util.Hardcoded
import org.scalatest.Ignore
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
      waitForUpdates()
      val image = getImage(imageId)
      image.tags.map(_.name).toSet should be(Set("nikon d7000", "2014-05-10", "all", "untagged"))
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
      waitForUpdates()
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
      waitForUpdates()
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
      waitForUpdates()
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
  it should "search popular images" in {
    authorized { implicit auth =>
      val imageId = createImage
      val image2 = createImage
      like(image2)
      waitForUpdates()
      val request = withCookie(Get(s"/api/images/popular?limit=1"))
      request ~> imagesRoute ~> check {
        status should be(StatusCodes.OK)
        val images = responseAs[GetImagesResponse].images
        images.length should be(1)
        images.head.id should be(image2)
      }
    }
  }
  it should "upload user avatar" in {
    authorized { implicit auth =>
      val user = getUser(auth.userId)
      uploadAvatar()
      waitForUpdates()
      val updated1 = getUser(auth.userId)
      uploadAvatar()
      waitForUpdates()
      val updated2 = getUser(auth.userId)
      Image.find(updated1.avatarId).isDefined should be(false)
      user.avatarId should not be(updated2.avatarId)
    }
  }


  private def uploadAvatar()(implicit auth: AuthResponse): Unit = {
    val request = withCookie(Post("/api/upload/avatar", MultipartFormData(Seq(BodyPart(dsc2845, "file")))))
    request ~> imagesRoute ~> check {
      status should be(StatusCodes.OK)
    }
  }

}
