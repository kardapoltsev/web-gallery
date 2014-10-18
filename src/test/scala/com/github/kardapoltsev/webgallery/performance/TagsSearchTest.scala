package com.github.kardapoltsev.webgallery.performance

import com.github.kardapoltsev.webgallery.TestBase
import com.github.kardapoltsev.webgallery.UserManager.SearchUsersResponse
import com.github.kardapoltsev.webgallery.http.{SearchSprayService, marshalling}
import com.github.kardapoltsev.webgallery.tags.TagsManager.GetTagsResponse
import org.scalatest.concurrent.Timeouts
import spray.http.{ContentTypes, StatusCodes}



/**
 * Created by alexey on 10/17/14.
 */
class TagsSearchTest extends TestBase with Timeouts with SearchSprayService {
  import com.github.kardapoltsev.webgallery.http.marshalling._
  import org.scalatest.time.SpanSugar._

  private val usersCount = if(isTravis) 10 else 20
  private val tagsCount = if(isTravis) 50 else 100
  private val tagsSearchTimeout = if(isTravis) 200 else 50

  behavior of "Tags search service"


  it should "quickly search users" in {
    for(u <- 1 to usersCount){
      authorizedRandomUser { implicit auth =>
        for(t <- 1 to tagsCount){
          createTag(randomString)
          if( t % 100 == 0){
            log.debug(s"created ${u * tagsCount + t} tags")
          }
        }
      }
      if(u % 100 == 0){
        log.debug(s"created $u users")
      }
    }
    val query = authorizedRandomUser { implicit auth =>
      val tagName = randomString
      createTag(tagName)
      tagName.take(3).toLowerCase
    }
    authorized { implicit auth =>
      failAfter(tagsSearchTimeout millis) {
        val request = withCookie(Get(s"/api/search/tags?term=$query"))
        request ~> searchRoute ~> check {
          status should be(StatusCodes.OK)
          contentType should be(ContentTypes.`application/json`)
          responseAs[GetTagsResponse].tags.isEmpty should be(false)
        }
      }
    }
  }

}
