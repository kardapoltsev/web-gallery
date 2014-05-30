package com.github.kardapoltsev.webgallery.http


import spray.routing.HttpService
import spray.testkit.ScalatestRouteTest
import org.scalatest.{FlatSpec, Matchers}
import akka.util.Timeout
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.Future
import spray.http.StatusCodes



/**
 * Created by alexey on 5/30/14.
 */
class SearchSprayServiceTest extends FlatSpec with Matchers with ScalatestRouteTest
  with HttpService with SearchSprayService {
    override def actorRefFactory = system
    override implicit val executionContext = system.dispatcher
    override implicit val requestTimeout = Timeout(FiniteDuration(3, concurrent.duration.SECONDS))

  override protected def searchTags(query: String): Future[Seq[String]] = Future.successful(Seq(query))

  "SearchSprayService" should "respond to /search/tags?query=test" in {
    Get("/search/tags?query=test") -> searchRoute -> check {
      status should be(StatusCodes.OK)
    }
  }
}
