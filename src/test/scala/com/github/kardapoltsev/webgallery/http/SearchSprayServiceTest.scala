package com.github.kardapoltsev.webgallery.http


import com.github.kardapoltsev.webgallery.UserManager.SearchUsersResponse
import com.github.kardapoltsev.webgallery.{TestBase}
import spray.http.{ContentTypes, StatusCodes}
import com.github.kardapoltsev.webgallery.TagsManager.{GetTagsResponse}



/**
 * Created by alexey on 5/30/14.
 */
class SearchSprayServiceTest extends TestBase with SearchSprayService {
  import marshalling._

  behavior of "SearchSprayService"

  it should "respond to /api/search/tags?query={query}" in {
    authorized { implicit auth =>
      val tag = createTag()
      val q = tag.name.take(2)
      val request = withCookie(Get(s"/api/search/tags?term=$q"))

      request ~> searchRoute ~> check {
        status should be(StatusCodes.OK)
        contentType should be(ContentTypes.`application/json`)
        responseAs[GetTagsResponse].tags.isEmpty should be(false)
      }
    }
  }

  it should "respond to /api/search/users?query={query}" in {
    authorized { implicit auth =>
      val userId2 = randomUserId
      val u2 = getUser(userId2)
      val q = u2.name.take(3).toLowerCase
      val request = withCookie(Get(s"/api/search/users?term=$q"))

      request ~> searchRoute ~> check {
        status should be(StatusCodes.OK)
        contentType should be(ContentTypes.`application/json`)
        responseAs[SearchUsersResponse].users.isEmpty should be(false)
      }
    }
  }

}
