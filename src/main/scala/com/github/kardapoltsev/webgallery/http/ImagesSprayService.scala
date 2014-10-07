package com.github.kardapoltsev.webgallery.http


import spray.routing.{Route, HttpService}
import scala.concurrent.{ExecutionContext}
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

  protected def updateImage(r: UpdateImage): Result[SuccessResponse] = processRequest(r)
  protected def getImage(r: GetImage): Result[GetImageResponse] = processRequest(r)
  protected def getByTag(r: GetByTag): Result[GetImagesResponse] = processRequest(r)
  protected def processNewImage(r: UploadImageRequest): Result[UploadImageResponse] = processRequest(r)
  protected def transformImage(r: TransformImageRequest): Result[TransformImageResponse] = processRequest(r)
  protected def getPopularImages(r: GetPopularImages.type): Result[GetImagesResponse] = processRequest(r)

  protected val lastModified = DateTime.now


  val imagesRoute: Route =
    pathPrefix("api") {
      (pathPrefix("images" / IntNumber / "file")
       & parameters('width.as[Option[Int]], 'height.as[Option[Int]], 'scaleType.as[String])) {(imageId, width, height, scale) =>
        get {
          conditional(EntityTag("alternative"), lastModified) {
            dynamic {
              handleWith(imageId :: width :: height :: scale :: HNil){
                transformImage
              }
            }
          }
          }
      } ~
      (path("images" / "popular") & get & offsetLimit) { (offset, limit) =>
        dynamic {
          handleWith(offset :: limit :: HNil)(getPopularImages)
        }
      } ~
      path("images" / IntNumber) { imageId =>
        patch {
          dynamic {
            handleWith(imageId :: HNil) {
              updateImage
            }
          }
        } ~ get {
          dynamic {
            handleWith(imageId :: HNil) {
              getImage
            }
          }
        }
      } ~
      (path("images") & parameters('tagId.as[Int]) & offsetLimit) { (tagId, offset, limit) =>
        get {
          dynamic {
            handleWith(tagId :: offset :: limit :: HNil) {
              getByTag
            }
          }
        }
      } ~
        path("upload") {
          post {
            entity(as[MultipartFormData]) { formData =>
              dynamic {
                handleWith(formData :: HNil) {
                  processNewImage
                }
              }
            }
          }
        }
    }
}
