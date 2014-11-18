package com.github.kardapoltsev.webgallery.http

import spray.routing.HttpService
import spray.testkit.ScalatestRouteTest
import org.scalatest.{ FlatSpec, Matchers }
import spray.http.StatusCodes

/**
 * Created by alexey on 6/4/14.
 */
class StaticSprayServiceTest extends FlatSpec with Matchers with ScalatestRouteTest
    with HttpService with StaticSprayService {

  override def actorRefFactory = system

  override protected val cwd = System.getProperty("user.dir")

  "StaticSprayService" should "respond to /" in {
    Get() ~> staticResourcesRoute ~> check {
      status should be(StatusCodes.OK)
    }
  }
  it should "serve static js resources" in {
    Get("/js/app.js") ~> staticResourcesRoute ~> check {
      status should be(StatusCodes.OK)
    }
  }
  it should "serve static css resources" in {
    Get("/css/main.css") ~> staticResourcesRoute ~> check {
      status should be(StatusCodes.OK)
    }
  }

}
