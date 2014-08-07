package com.github.kardapoltsev.webgallery.http


import spray.routing.{Route, HttpService}
import scala.concurrent.{Future, ExecutionContext}
import akka.util.Timeout
import com.github.kardapoltsev.webgallery.db.{Alternative, Image}
import java.io.{FileOutputStream, File}
import com.github.kardapoltsev.webgallery.Configs
import spray.http._
import spray.json._
import com.github.kardapoltsev.webgallery.Database._
import com.github.kardapoltsev.webgallery.processing.{OptionalSize, ScaleType, SpecificSize}
import com.github.kardapoltsev.webgallery.dto.ImageInfo
import shapeless._
import com.github.kardapoltsev.webgallery.Database.UpdateImage
import com.github.kardapoltsev.webgallery.ImageProcessor.TransformImageRequest


/**
 * Created by alexey on 6/4/14.
 */
trait ImagesSprayService extends BaseSprayService { this: HttpService =>
  import marshalling._
  import BaseSprayService._

  import spray.routing.directives.CachingDirectives._
  implicit def executionContext: ExecutionContext
  implicit def requestTimeout: Timeout

  protected def updateImage(r: UpdateImage): Result[SuccessResponse] = processRequest(r)
  protected def getImage(r: GetImage): Result[GetImageResponse] = processRequest(r)
  protected def getByTag(r: GetByTag): Result[GetImagesResponse] = processRequest(r)

  protected def transformImage(request: TransformImageRequest): Future[Alternative]


  val imagesRoute: Route =
    pathPrefix("api") {
      (pathPrefix("images" / IntNumber / "file")
       & parameters('width.as[Option[Int]], 'height.as[Option[Int]], 'scaleType.as[String])) {(imageId, width, height, scale) =>
        complete {
          val scaleType = ScaleType.withName(scale)
          transformImage(TransformImageRequest(imageId, OptionalSize(width, height, scaleType))) map {
            case alternative =>
              HttpResponse(StatusCodes.OK,
                HttpEntity(ContentType(MediaTypes.`image/jpeg`),
                  HttpData.fromFile(Configs.AlternativesDir + alternative.filename)))
          }
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
      (path("images") & parameters('tag)) { tagName =>
        get {
          dynamic {
            handleWith(tagName :: HNil) {
              getByTag
            }
          }
        }
      } ~
        path("upload") {
          post {
            entity(as[MultipartFormData]) { formData =>
              val filePart = formData.fields.head
              val filename = filePart.headers.find(h => h.is("content-disposition")).get.value.split("filename=").last
              saveAttachment(filename, filePart.entity.data.toByteArray)
              complete(JsObject("name" -> JsString(filename)).compactPrint)
            }
          }
        }
    }


  protected def saveAttachment(filename: String, content: Array[Byte]): Unit = {
    val fos = new FileOutputStream(Configs.UnprocessedDir + "/" + filename)
    try {
      fos.write(content)
    } finally {
      fos.close()
    }
  }
}
