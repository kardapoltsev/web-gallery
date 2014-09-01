package com.github.kardapoltsev.webgallery.http


import com.github.kardapoltsev.webgallery.TestBase
import com.github.kardapoltsev.webgallery.TagsManager._
import spray.http.{StatusCodes}


/**
 * Created by alexey on 5/28/14.
 */
class TagsSprayServiceTest extends TestBase with TagsSprayService {
  import marshalling._

  behavior of "TagsSprayService"

  it should "create tags" in {
    authorized { implicit auth =>
      createTag()
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

}
