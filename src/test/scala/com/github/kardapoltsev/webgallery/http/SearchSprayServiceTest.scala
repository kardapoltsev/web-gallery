package com.github.kardapoltsev.webgallery.http


import com.github.kardapoltsev.webgallery.UserManager.SearchUsersResponse
import com.github.kardapoltsev.webgallery.tags.TagsManager
import com.github.kardapoltsev.webgallery.{TestBase}
import spray.http.{ContentTypes, StatusCodes}
import TagsManager.{GetTagsResponse}



/**
 * Created by alexey on 5/30/14.
 */
class SearchSprayServiceTest extends TestBase with SearchSprayService {
  import marshalling._

  behavior of "SearchSprayService"

  it should "respond to /api/search/tags?query={query}" in {
    authorized { implicit auth =>
      val tag = createTag("tag1")
      createTag("tag2")
      val q = tag.name.take(2)
      val limit = 1
      val request = withCookie(Get(s"/api/search/tags?term=$q&limit=$limit"))

      request ~> searchRoute ~> check {
        status should be(StatusCodes.OK)
        contentType should be(ContentTypes.`application/json`)
        val response = responseAs[GetTagsResponse]
        response.tags.isEmpty should be(false)
        response.tags.length <= limit should be(true)
      }
    }
  }

  it should "respond to /api/search/users?query={query}" in {
    authorized { implicit auth =>
      val limit = 1
      val userId2 = randomUserId
      val u2 = getUser(userId2)
      val q = u2.name.take(3).toLowerCase
      val request = withCookie(Get(s"/api/search/users?term=$q&limit=$limit"))

      request ~> searchRoute ~> check {
        status should be(StatusCodes.OK)
        contentType should be(ContentTypes.`application/json`)
        val response = responseAs[SearchUsersResponse]
        response.users.isEmpty should be(false)
        response.users.length <= limit should be(true)
      }
    }
  }

}
