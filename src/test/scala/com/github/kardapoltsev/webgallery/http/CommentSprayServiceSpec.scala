package com.github.kardapoltsev.webgallery.http

import com.github.kardapoltsev.webgallery.CommentManager.{ GetCommentsResponse, AddCommentResponse }
import com.github.kardapoltsev.webgallery.TestBase
import com.github.kardapoltsev.webgallery.UserManager.AuthResponse
import com.github.kardapoltsev.webgallery.db._
import spray.http.{ ContentTypes, HttpEntity, StatusCodes }

/**
 * Created by alexey on 5/30/14.
 */
class CommentSprayServiceSpec extends TestBase with CommentSprayService {

  import marshalling._

  behavior of "SearchSprayService"

  it should "add comment via POST /api/images/{imageId}/comments" in {
    authorized { implicit auth =>
      val imageId = createImage
      createComment(imageId)
    }
  }

  it should "return comments via GET /api/images/{imageId}/comments" in {
    authorized { implicit auth =>
      val imageId = createImage
      val comment = createComment(imageId)
      createComment(imageId, Some(comment.id))
      getComments(imageId).length should be(2)
    }
  }

  it should "return comments with limit and offset" in {
    authorized { implicit auth =>
      val imageId = createImage
      val commentsCount = 30
      for (i <- 1 to commentsCount) {
        createComment(imageId)
      }
      getComments(imageId, Some(0), Some(commentsCount)).length should be(commentsCount)

      getComments(imageId, Some(4), Some(5)).length should be(5)
    }
  }

  it should "delete comment" in {
    authorized { implicit auth =>
      val imageId = createImage
      val comment = createComment(imageId)
      deleteComment(imageId, comment.id)

    }
  }

  it should "create comment and reply comment, delete comment" in {
    val comment = authorized { implicit auth =>
      val imageId = createImage
      val comment = createComment(imageId)
      createComment(imageId, Some(comment.id))
      getComments(imageId, Some(0), Some(1)).length should be(1)
      comment
    }
    authorizedRandomUser { implicit auth =>
      val request = withCookie(Delete(s"/api/images/${comment.imageId}/comments/${comment.id}"))
      request ~> commentRoute ~> check {
        status should be(StatusCodes.Forbidden)
      }
    }
  }

  private def createComment(
    imageId: ImageId,
    parentId: Option[CommentId] = None)(implicit auth: AuthResponse): Comment = {
    val request =
      withCookie(Post(s"/api/images/$imageId/comments",
        HttpEntity(ContentTypes.`application/json`, AddCommentBody("test comment", parentId).toJson.compactPrint)))

    request ~> commentRoute ~> check {
      status should be(StatusCodes.OK)
      contentType should be(ContentTypes.`application/json`)
      responseAs[AddCommentResponse].comment
    }
  }

  private def getComments(imageId: ImageId, offset: Option[Int] = None, limit: Option[Int] = None)(implicit auth: AuthResponse): Seq[CommentInfo] = {

    val o = offset.map(o => s"offset=$o").getOrElse("")
    val l = limit.map(l => s"limit=$l").getOrElse("")
    val request = withCookie(Get(s"/api/images/$imageId/comments?$o&$l"))
    request ~> commentRoute ~> check {
      status should be(StatusCodes.OK)
      contentType should be(ContentTypes.`application/json`)
      responseAs[GetCommentsResponse].comments
    }
  }

  private def deleteComment(imageId: ImageId, commentId: CommentId)(implicit auth: AuthResponse): Unit = {
    val request = withCookie(Delete(s"/api/images/$imageId/comments/$commentId"))
    request ~> commentRoute ~> check {
      status should be(StatusCodes.OK)
    }
  }

}

