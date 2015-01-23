package com.github.kardapoltsev.webgallery.http

import com.github.kardapoltsev.webgallery.StatsManager.{ GetStats, GetStatsResponse }
import com.github.kardapoltsev.webgallery.db.{ Database, Stats }
import com.github.kardapoltsev.webgallery.{ StatsManager, TestBase }
import spray.http.{ ContentTypes, StatusCodes }

/**
 * Created by koko on 22/01/15.
 */
class StatsSprayServiceSpec extends TestBase with StatsSprayService {
  import marshalling._

  Database.cleanDatabase()

  it should "return statistic" in {
    authorized { implicit auth =>
      val imageId = createImage
      like(imageId)
      createComment(imageId)

      val stats = processGetStats().stats
      val expected = Stats(3, 3, 1, 1) // 3 Users: anonymous and root , 3 Images : default avatar and default cover
      stats should be(expected)

    }
  }

  private def processGetStats(): GetStatsResponse = {
    val request = Get("/api/stats")
    request ~> statisticRoute ~> check {
      status should be(StatusCodes.OK)
      contentType should be(ContentTypes.`application/json`)
      responseAs[GetStatsResponse]
    }
  }

}

