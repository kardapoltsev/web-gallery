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

  protected def updateImage(r: UpdateImage) = processRequest(r)
  protected def getImage(r: GetImage) = processRequest(r)
  protected def deleteImage(r: DeleteImage) = processRequest(r)
  protected def getByTag(r: GetByTag) = processRequest(r)
  protected def processNewImage(r: UploadImage) = processRequest(r)
  protected def uploadAvatar(r: UploadAvatar) = processRequest(r)
  protected def transformImage(r: TransformImageRequest) = processRequest(r)
  protected def getPopularImages(r: GetPopularImages) = processRequest(r)

  protected val lastModified = DateTime.now

  val imagesRoute: Route =
    pathPrefix("api") {
      (pathPrefix("images" / IntNumber / "file")
        & parameters('width.as[Option[Int]], 'height.as[Option[Int]], 'scaleType.as[String])) { (imageId, width, height, scale) =>
          get {
            conditional(EntityTag("alternative"), lastModified) {
              dynamic {
                handleRequest(imageId :: width :: height :: scale :: HNil) {
                  transformImage
                }
              }
            }
          }
        } ~
        (path("images" / "popular") & get & offsetLimit) { (offset, limit) =>
          dynamic {
            handleRequest(offset :: limit :: HNil)(getPopularImages)
          }
        } ~
        path("images" / IntNumber) { imageId =>
          patch {
            dynamic {
              handleRequest(imageId :: HNil) {
                updateImage
              }
            }
          } ~
            get {
              dynamic {
                handleRequest(imageId :: HNil) {
                  getImage
                }
              }
            } ~
            delete {
              dynamic {
                handleRequest(imageId :: HNil) {
                  deleteImage
                }
              }
            }
        } ~
        (path("images") & parameters('tagId.as[Int]) & offsetLimit) { (tagId, offset, limit) =>
          get {
            dynamic {
              handleRequest(tagId :: offset :: limit :: HNil) {
                getByTag
              }
            }
          }
        } ~
        (pathPrefix("upload") & post) {
          pathEnd {
            entity(as[MultipartFormData]) { formData =>
              dynamic {
                handleRequest(formData :: HNil) {
                  processNewImage
                }
              }
            }
          } ~
            path("avatar") {
              entity(as[MultipartFormData]) { formData =>
                dynamic {
                  handleRequest(formData :: HNil) {
                    uploadAvatar
                  }
                }
              }
            }
        }
    }
}
