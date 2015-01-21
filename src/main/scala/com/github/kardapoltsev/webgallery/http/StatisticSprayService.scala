package com.github.kardapoltsev.webgallery.http

import com.github.kardapoltsev.webgallery.{ ApplicationMode, Configs }
import spray.routing.directives.ClassMagnet
import spray.routing.{ Route, HttpService }
import spray.http._

/**
 * Created by alexey on 6/4/14.
 */
trait StatisticSprayService { this: HttpService =>

  val statisticRoute: Route =
    path("stats") {
      optionalHeaderValueByType(ClassMagnet[HttpHeaders.`Accept-Language`](())) { header =>
        val lang = header.map(_.value.split(",").head.trim).getOrElse("en")
        implicit val language = Language(lang)
        respondWithMediaType(MediaTypes.`text/html`) {
          complete(html.stats().toString)
        }
      }
    }
}
