package com.github.kardapoltsev.webgallery.http

import com.github.kardapoltsev.webgallery.StatsManager.{ GetStatsResponse, GetStats }
import spray.routing.{ Route, HttpService }
import spray.http._

/**
 * Created by alexey on 6/4/14.
 */
trait StatsSprayService extends BaseSprayService { this: HttpService =>
  import marshalling._

  val statisticRoute: Route =
    path("api" / "stats") {
      perRequest[GetStats, GetStatsResponse]
    }

}
