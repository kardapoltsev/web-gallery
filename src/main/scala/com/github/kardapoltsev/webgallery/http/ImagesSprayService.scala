package com.github.kardapoltsev.webgallery.http

import spray.routing.{ Route, HttpService }
import scala.concurrent.{ ExecutionContext }
import akka.util.Timeout
import spray.http._
import shapeless._

/**
 * Created by alexey on 6/4/14.
 */
trait ImagesSprayService extends BaseSprayService { this: HttpService =>
  import marshalling._
  import BaseSprayService._
  import com.github.kardapoltsev.webgallery.ImageManager._
  import com.github.kardapoltsev.webgallery.ImageHolder._

  import spray.routing.directives.CachingDirectives._
  implicit def executionContext: ExecutionContext
  implicit def requestTimeout: Timeout

  protected val lastModified = DateTime.now

  val imagesRoute: Route =
    pathPrefix("api") {
      (pathPrefix("images" / IntNumber / "file")
        & parameters('width.as[Option[Int]], 'height.as[Option[Int]], 'scaleType.as[String])) { (imageId, width, height, scale) =>
          get {
            conditional(EntityTag("alternative"), lastModified) {
              perRequest(imageId :: width :: height :: scale :: HNil) {
                r: TransformImageRequest => HandlerWrapper[TransformImageResponse](r)
              }
            }
          }
        } ~
        (path("images" / "popular") & get & offsetLimit) { (offset, limit) =>
          perRequest(offset :: limit :: HNil) {
            createWrapper[GetPopularImages, GetImagesResponse]
          }
        } ~
        path("images" / IntNumber) { imageId =>
          patch {
            perRequest(imageId :: HNil) {
              createWrapper[UpdateImage, SuccessResponse]
            }
          } ~
            get {
              perRequest(imageId :: HNil) {
                createWrapper[GetImage, GetImageResponse]
              }
            } ~
            delete {
              perRequest(imageId :: HNil) {
                createWrapper[DeleteImage, SuccessResponse]
              }
            }
        } ~
        (path("images") & parameters('tagId.as[Int]) & offsetLimit) { (tagId, offset, limit) =>
          get {
            perRequest(tagId :: offset :: limit :: HNil) {
              createWrapper[GetByTag, GetImagesResponse]
            }
          }
        } ~
        (pathPrefix("upload") & post) {
          pathEnd {
            entity(as[MultipartFormData]) { formData =>
              perRequest(formData :: HNil) {
                createWrapper[UploadImage, UploadImageResponse]
              }
            }
          } ~
            path("avatar") {
              entity(as[MultipartFormData]) { formData =>
                perRequest(formData :: HNil) {
                  createWrapper[UploadAvatar, SuccessResponse]
                }
              }
            }
        }
    }
}
