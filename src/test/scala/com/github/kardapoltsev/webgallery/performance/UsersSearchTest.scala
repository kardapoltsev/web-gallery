package com.github.kardapoltsev.webgallery.performance


import com.github.kardapoltsev.webgallery.TestBase
import com.github.kardapoltsev.webgallery.UserManager.SearchUsersResponse
import com.github.kardapoltsev.webgallery.http.{marshalling, SearchSprayService}
import org.scalatest.concurrent.Timeouts
import spray.http.{ContentTypes, StatusCodes}



/**
 * Created by alexey on 10/17/14.
 */
class UsersSearchTest extends TestBase with Timeouts with SearchSprayService {
  import org.scalatest.time.SpanSugar._
  import marshalling._

  behavior of "UserManager"

  private val usersCount = if(isTravis) 500 else 5000
  private val searchTimeout = if(isTravis) 100 else 50

  it should "quickly search users" in {
    for(i <- 1 to usersCount){
      if(i % 100 == 0){
        log.debug(s"created $i users")
      }
      authorizedRandomUser { _ => }
    }
    val query = authorizedRandomUser { implicit auth =>
      val user = getUser(auth.userId)
      user.name.take(3).toLowerCase
    }
    authorized { implicit auth =>
      failAfter(searchTimeout millis) {
        val request = withCookie(Get(s"/api/search/users?term=$query"))
        request ~> searchRoute ~> check {
          status should be(StatusCodes.OK)
          contentType should be(ContentTypes.`application/json`)
          responseAs[SearchUsersResponse].users.isEmpty should be(false)
        }
      }
    }
  }

}
