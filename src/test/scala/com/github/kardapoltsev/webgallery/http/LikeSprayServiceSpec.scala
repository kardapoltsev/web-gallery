package com.github.kardapoltsev.webgallery.http

import com.github.kardapoltsev.webgallery.TestBase
import spray.http.{ContentTypes, StatusCodes}



/**
 * Created by alexey on 5/30/14.
 */
class LikeSprayServiceSpec extends TestBase with LikeSprayService {


  behavior of "LikeSprayService"

  it should "respond to POST /api/images/{imageId}/likes" in {
    authorized { implicit auth =>
      val imageId = createImage
      val request = withCookie(Post(s"/api/images/$imageId/likes"))

      request ~> likeRoute ~> check {
        status should be(StatusCodes.OK)
      }
    }
  }

  it should "respond to DELETE /api/images/{imageId}/likes" in {
    authorized { implicit auth =>
      val imageId = createImage
      val request = withCookie(Delete(s"/api/images/$imageId/likes"))

      request ~> likeRoute ~> check {
        status should be(StatusCodes.OK)
      }
    }
  }
}
