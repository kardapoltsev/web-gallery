package com.github.kardapoltsev.webgallery.http

import com.github.kardapoltsev.webgallery.CommentManager.{GetCommentsResponse, AddCommentResponse}
import com.github.kardapoltsev.webgallery.TestBase
import com.github.kardapoltsev.webgallery.UserManager.AuthResponse
import com.github.kardapoltsev.webgallery.db._
import spray.http.{ContentTypes, HttpEntity, StatusCodes}



/**
 * Created by alexey on 5/30/14.
 */
class CommentSprayServiceSpec extends TestBase with CommentSprayService {

  import marshalling._


  behavior of "SearchSprayService"

  it should "respond to POST /api/images/{imageId}/comments" in {
    authorized { implicit auth =>
      val imageId = createImage
      createComment(imageId)
    }
  }

  it should "respond to GET /api/images/{imageId}/comments" in {
    authorized { implicit auth =>
      val imageId = createImage
      createComment(imageId)
      getComments(imageId).length should be(1)
    }
  }


  private def createComment(imageId: ImageId)(implicit auth: AuthResponse): Comment = {
    val request =
      withCookie(Post(s"/api/images/$imageId/comments",
        HttpEntity(ContentTypes.`application/json`, AddCommentBody("test comment", None).toJson.compactPrint)))

    request ~> commentRoute ~> check {
      status should be(StatusCodes.OK)
      contentType should be(ContentTypes.`application/json`)
      responseAs[AddCommentResponse].comment
    }
  }


  private def getComments(imageId: ImageId)(implicit auth: AuthResponse): Seq[CommentDto] = {
    val request = withCookie(Get(s"/api/images/$imageId/comments"))
    request ~> commentRoute ~> check {
      status should be(StatusCodes.OK)
      contentType should be(ContentTypes.`application/json`)
      responseAs[GetCommentsResponse].comments
    }
  }

}
