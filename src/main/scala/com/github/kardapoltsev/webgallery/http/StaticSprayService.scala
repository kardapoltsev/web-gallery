package com.github.kardapoltsev.webgallery.http

import spray.routing.{Route, HttpService}
import spray.http._


/**
 * Created by alexey on 6/4/14.
 */
trait StaticSprayService { this: HttpService =>
  def cwd: String

  val staticResourcesRoute: Route =
    pathPrefix("js") {
      getFromDirectory(cwd + "/web/js")
    } ~
    pathPrefix("css") {
      getFromDirectory(cwd + "/web/css")
    } ~
    respondWithMediaType(MediaTypes.`text/html`) {
      pathPrefix(!"api"){
        getFromFile(cwd + "/web/index.html")
      }
    }
}
