package com.github.kardapoltsev.webgallery.http

import com.github.kardapoltsev.webgallery.TestBase
import com.github.kardapoltsev.webgallery.UserManager.AuthResponse
import com.github.kardapoltsev.webgallery.db.ImageId
import spray.http.{ContentTypes, StatusCodes}



/**
 * Created by alexey on 5/30/14.
 */
class LikeSprayServiceSpec extends TestBase with LikeSprayService {


  behavior of "LikeSprayService"

  it should "like image via POST /api/images/{imageId}/likes" in {
    authorized { implicit auth =>
      val imageId = createImage
      like(imageId)
      val image = getImage(imageId)
      image.likesCount should be(1)
      image.isLiked should be(true)
    }
  }

  it should "not like twice" in {
    authorized { implicit auth =>
      val imageId = createImage
      like(imageId)

      val request = withCookie(Post(s"/api/images/$imageId/likes"))
      request ~> likeRoute ~> check {
        status should be(StatusCodes.UnprocessableEntity)
      }
      val image = getImage(imageId)
      image.likesCount should be(1)
      image.isLiked should be(true)
    }
  }

  it should "not like non existing image" in {
    authorized { implicit auth =>
      val imageId = 100500
      val request = withCookie(Post(s"/api/images/$imageId/likes"))
      request ~> likeRoute ~> check {
        status should be(StatusCodes.NotFound)
      }
    }
  }

  it should "unlike image via DELETE /api/images/{imageId}/likes" in {
    authorized { implicit auth =>
      val imageId = createImage
      like(imageId)
      val request = withCookie(Delete(s"/api/images/$imageId/likes"))
      request ~> likeRoute ~> check {
        status should be(StatusCodes.OK)
      }
      val image = getImage(imageId)
      image.likesCount should be(0)
      image.isLiked should be(false)
    }
  }

  private def like(imageId: ImageId)(implicit auth: AuthResponse): Unit = {
    val request = withCookie(Post(s"/api/images/$imageId/likes"))
    request ~> likeRoute ~> check {
      status should be(StatusCodes.OK)
    }
  }

}
