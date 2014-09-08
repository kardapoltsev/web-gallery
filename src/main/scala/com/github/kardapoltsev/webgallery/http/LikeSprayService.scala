package com.github.kardapoltsev.webgallery.http


import akka.util.Timeout
import com.github.kardapoltsev.webgallery.ImageHolder.{UnlikeImage, LikeImage}
import shapeless.HNil
import spray.routing.{HttpService, Route}

import scala.concurrent.ExecutionContext



/**
 * Created by alexey on 8/28/14.
 */
trait LikeSprayService extends BaseSprayService { this: HttpService =>
  import marshalling._
  import BaseSprayService._

  implicit def executionContext: ExecutionContext
  implicit def requestTimeout: Timeout

  protected def like(r: LikeImage): Result[SuccessResponse] = processRequest(r)
  protected def unlike(r: UnlikeImage): Result[SuccessResponse] = processRequest(r)



  val likeRoute: Route =
    pathPrefix("api" / "images") {
      path(IntNumber / "likes") { imageId =>
        post {
          dynamic {
            handleWith(imageId :: HNil) {
              like
            }
          }
        } ~
        delete {
          dynamic{
            handleWith(imageId :: HNil){
              unlike
            }
          }
        }
      }
    }
}
