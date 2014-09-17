package com.github.kardapoltsev.webgallery.http

import spray.routing.{Route, HttpService}
import spray.http._


/**
 * Created by alexey on 6/4/14.
 */
trait StaticSprayService { this: HttpService =>
  def cwd: String

  val appDir = "app-build"
  val staticResourcesRoute: Route =
    pathPrefix("js") {
      getFromDirectory(cwd + "/web/" + appDir + "/js")
    } ~
    pathPrefix("css") {
      getFromDirectory(cwd + "/web/" + appDir + "/css")
    } ~
    pathPrefix("static") {
       getFromDirectory(cwd + "/web/static")
    } ~
    respondWithMediaType(MediaTypes.`text/html`) {
      pathPrefix(!"api"){
        respondWithMediaType(MediaTypes.`text/html`) {
          complete(html.index().toString)
        }
      }
    }
}
