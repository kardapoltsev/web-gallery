package com.github.kardapoltsev.webgallery.http

import akka.util.Timeout
import com.github.kardapoltsev.webgallery.ImageHolder.{ UnlikeImage, LikeImage }
import shapeless.HNil
import spray.routing.{ HttpService, Route }

import scala.concurrent.ExecutionContext

/**
 * Created by alexey on 8/28/14.
 */
trait LikeSprayService extends BaseSprayService { this: HttpService =>
  import marshalling._
  import BaseSprayService._

  implicit def executionContext: ExecutionContext
  implicit def requestTimeout: Timeout

  val likeRoute: Route =
    pathPrefix("api" / "images") {
      path(IntNumber / "likes") { imageId =>
        post {
          perRequest(imageId :: HNil) {
            createWrapper[LikeImage, SuccessResponse]
          }
        } ~
          delete {
            perRequest(imageId :: HNil) {
              createWrapper[UnlikeImage, SuccessResponse]
            }
          }
      }
    }

}
