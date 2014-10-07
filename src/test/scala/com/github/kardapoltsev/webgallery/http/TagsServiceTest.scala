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

  it should "returt user tags" in {
    authorized { implicit auth =>
      getUserTags(auth.userId)
    }
  }

  it should "create default user tags" in {
    authorized { implicit auth =>
      Thread.sleep(2000L)
      getUserTags(auth.userId).length should be(2)
    }
  }

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
      Thread.sleep(2000L) // wait for cover update
      val update = getTag(tag.ownerId, tag.id)
      update.coverId should be(imageId)
    }
  }

  it should "update tag name" in {
    authorized { implicit auth =>
      val tag = createTag()
      val newName = "newTagName"
      updateTag(tag.id, UpdateTagBody(Some(newName), None))
      val updated = getTag(auth.userId, tag.id)
      updated.name should be(newName)
    }
  }

  it should "not change manually set tag coverId" in {
    authorized { implicit auth =>
      val tag = createTag()
      val imageId = createImage
      addTag(imageId, tag)

      val newCoverId = createImage

      //check that coverId is updated
      updateTag(tag.id, UpdateTagBody(None, Some(newCoverId)))
      val updated = getTag(auth.userId, tag.id)
      updated.coverId should be(newCoverId)

      //check that manually set cover wasn't replaced by new tagger image id
      val newImageId = createImage
      addTag(newImageId, tag)
      Thread.sleep(2000L) // wait for cover update
      val updated2 = getTag(auth.userId, tag.id)
      updated2.coverId should be(newCoverId)
    }
  }


  private def updateTag(tagId: TagId, params: UpdateTagBody)(implicit auth: AuthResponse): Unit = {
    val request = withCookie(Patch(s"/api/users/${auth.userId}/tags/$tagId", params))
    request ~> tagsRoute ~> check {
      status should be(StatusCodes.OK)
    }
  }


  private def getTag(userId: UserId, tagId: TagId)(implicit auth: AuthResponse): Tag = {
    withCookie(Get(s"/api/users/$userId/tags/$tagId")) ~> tagsRoute ~> check {
      status should be(StatusCodes.OK)
      responseAs[GetTagResponse].tag
    }
  }


  private def getUserTags(userId: UserId)(implicit auth: AuthResponse): Seq[Tag] = {
    withCookie(Get(s"/api/users/$userId/tags")) ~> tagsRoute ~> check {
      status should be(StatusCodes.OK)
      responseAs[GetTagsResponse].tags
    }
  }

}
