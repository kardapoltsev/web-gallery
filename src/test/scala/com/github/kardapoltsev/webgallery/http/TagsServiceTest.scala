package com.github.kardapoltsev.webgallery.http


import com.github.kardapoltsev.webgallery.TestBase
import com.github.kardapoltsev.webgallery.UserManager.AuthResponse
import com.github.kardapoltsev.webgallery.db.{UserId, TagId, Tag}
import com.github.kardapoltsev.webgallery.tags.TagsManager
import TagsManager._
import com.github.kardapoltsev.webgallery.util.Hardcoded
import spray.http.{StatusCodes}


/**
 * Created by alexey on 5/28/14.
 */
class TagsServiceTest extends TestBase with TagsSprayService {
  import marshalling._

  behavior of "TagsSprayService"

  it should "create tags" in {
    authorized { implicit auth =>
      createTag()
    }
  }

  it should "return tag by id" in {
    authorized { implicit auth =>
      val tag = createTag()
      val tag2 = getTag(tag.ownerId, tag.id)
      tag2.name should be(tag.name)
    }
  }

  it should "return user tags" in {
    authorized { implicit auth =>
      val tag = createTag()
      val request = withCookie(Get(s"/api/users/${auth.userId}/tags"))

      request ~> tagsRoute ~> check {
        status should be(StatusCodes.OK)
        responseAs[GetTagsResponse].tags.isEmpty should be(false)
      }
    }
  }

  it should "return recent tags" in {
    authorized { implicit auth =>
      val tag = createTag()
      val request = withCookie(Get(s"/api/users/${auth.userId}/tags/recent"))
      request ~> tagsRoute ~> check {
        status should be(StatusCodes.OK)
        responseAs[GetTagsResponse].tags.isEmpty should be(false)
      }
    }
  }

  it should "return recent tags with limit" in {
    authorized { implicit auth =>
      for(i <- 1 to 5) {
        createTag(s"tag-$i")
      }
      val limit = 3
      val request = withCookie(Get(s"/api/users/${auth.userId}/tags/recent?limit=$limit"))
      request ~> tagsRoute ~> check {
        status should be(StatusCodes.OK)
        responseAs[GetTagsResponse].tags.length should be(limit)
      }
    }
  }

  it should "set tag coverId to default" in {
    authorized { implicit auth =>
      val tag = createTag()
      tag.coverId should be(Hardcoded.DefaultCoverId)
    }
  }

  it should "update coverId when tag is added to image" in {
    authorized { implicit auth =>
      val tag = createTag()
      tag.coverId should be(Hardcoded.DefaultCoverId)
      val imageId = createImage
      addTag(imageId, tag)
      val update = getTag(tag.ownerId, tag.id)
    }
  }


  private def getTag(userId: UserId, tagId: TagId)(implicit auth: AuthResponse): Tag = {
    withCookie(Get(s"/api/users/$userId/tags/$tagId")) ~> tagsRoute ~> check {
      status should be(StatusCodes.OK)
      responseAs[GetTagResponse].tag
    }
  }
}
