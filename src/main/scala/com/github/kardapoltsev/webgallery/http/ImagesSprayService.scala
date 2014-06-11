package com.github.kardapoltsev.webgallery.http


import spray.routing.{Route, HttpService}
import scala.concurrent.{Future, ExecutionContext}
import akka.util.Timeout
import com.github.kardapoltsev.webgallery.db.{Alternative, Image}
import java.io.{FileOutputStream, File}
import com.github.kardapoltsev.webgallery.Configs
import spray.http._
import spray.json._
import com.github.kardapoltsev.webgallery.Database.{UpdateImageParams, UpdateImage}
import scala.Some
import spray.http.HttpResponse
import com.github.kardapoltsev.webgallery.Database.UpdateImage
import com.github.kardapoltsev.webgallery.Database.UpdateImageParams
import com.github.kardapoltsev.webgallery.ImageProcessor.TransformImageRequest
import com.github.kardapoltsev.webgallery.processing.{ScaleType, SpecificSize}
import com.github.kardapoltsev.webgallery.dto.ImageInfo



/**
 * Created by alexey on 6/4/14.
 */
trait ImagesSprayService { this: HttpService =>
  import marshalling._
//  import spray.httpx.SprayJsonSupport._

  import spray.routing.directives.CachingDirectives._
  implicit def executionContext: ExecutionContext
  implicit def requestTimeout: Timeout

  protected def getByTag(tagName: String): Future[Seq[ImageInfo]]
  protected def updateImage(request: UpdateImage): Future[InternalResponse]
  protected def getImage(imageId: Int): Future[Option[ImageInfo]]
  protected def transformImage(request: TransformImageRequest): Future[Alternative]


  val imagesRoute: Route =
    pathPrefix("images" / Segment / "original") { filename =>
      getFromFile(new File(Configs.OriginalsDir + "/" + filename))
    } ~
    (pathPrefix("images" / IntNumber)
      & parameters('width.as[Int], 'height.as[Int], 'crop.as[Boolean])) {(imageId, width, height, crop) =>
      complete {
        val scaleType = if(crop) ScaleType.FillDest else ScaleType.FitSource
        transformImage(TransformImageRequest(imageId, SpecificSize(width, height, scaleType))) map {
          case alternative =>
            HttpResponse(StatusCodes.OK,
              HttpEntity(ContentType(MediaTypes.`image/jpeg`),
                HttpData.fromFile(Configs.AlternativesDir + alternative.filename)))
        }
      }
    } ~
    pathPrefix("api") {
      path("images" / IntNumber) { imageId =>
        patch {
          entity(as[UpdateImageParams]) { request => ctx =>
            updateImage(UpdateImage(imageId, request)) map {
              case SuccessResponse => ctx.complete(HttpResponse(StatusCodes.OK))
            }
          }
        } ~ get {
          complete {
            getImage(imageId) map {
              case Some(image) => HttpResponse(StatusCodes.OK, image.toJson.compactPrint)
              case None => HttpResponse(StatusCodes.NotFound)
            }
          }
        }
      } ~
      (path("images") & parameters('tag)) { tagName =>
        get { ctx =>
          getByTag(tagName) map {
            case images => ctx.complete(images.toJson.compactPrint)
            }
          }
        } ~
        path("upload") {
          post {
            entity(as[MultipartFormData]) { formData =>
              val filePart = formData.fields.head
              val fileName = filePart.headers.find(h => h.is("content-disposition")).get.value.split("filename=").last
              saveAttachment(fileName, filePart.entity.data.toByteArray)
              redirect("/", StatusCodes.MovedPermanently)
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
