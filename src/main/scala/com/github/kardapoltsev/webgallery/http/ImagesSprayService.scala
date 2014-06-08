package com.github.kardapoltsev.webgallery.http


import spray.routing.{Route, HttpService}
import scala.concurrent.{Future, ExecutionContext}
import akka.util.Timeout
import com.github.kardapoltsev.webgallery.db.{Alternative, TransformImageParams, Image}
import java.io.{FileOutputStream, File}
import com.github.kardapoltsev.webgallery.Configs
import spray.http._
import spray.json._
import com.github.kardapoltsev.webgallery.Database.{UpdateImageParams, SuccessResponse, InternalResponse, UpdateImage}
import com.github.kardapoltsev.webgallery.ImageProcessor.{TransformImageResponse, TransformImageRequest}
import scala.util.{Success, Failure}
import scala.util.Failure
import scala.Some
import com.github.kardapoltsev.webgallery.db.TransformImageParams
import spray.http.HttpResponse
import com.github.kardapoltsev.webgallery.Database.UpdateImage
import scala.util.Success
import com.github.kardapoltsev.webgallery.Database.UpdateImageParams
import com.github.kardapoltsev.webgallery.ImageProcessor.TransformImageRequest


/**
 * Created by alexey on 6/4/14.
 */
trait ImagesSprayService { this: HttpService =>
  import marshalling._

  import spray.routing.directives.CachingDirectives._
  implicit def executionContext: ExecutionContext
  implicit def requestTimeout: Timeout

  protected def getByTag(tagName: String): Future[Seq[Image]]
  protected def updateImage(request: UpdateImage): Future[InternalResponse]
  protected def getImage(imageId: Int): Future[Option[Image]]
  protected def transformImage(request: TransformImageRequest): Future[Alternative]


  val imagesRoute: Route =
    pathPrefix("images" / Segment / "original") { filename =>
      getFromFile(new File(Configs.OriginalsDir + "/" + filename))
    } ~
    (pathPrefix("images" / IntNumber)
      & parameters('width.as[Int], 'height.as[Int], 'crop.as[Boolean])) {(imageId, width, height, crop) =>
      complete {
        transformImage(TransformImageRequest(imageId, TransformImageParams(width, height, crop))) map {
          case alternative =>
            HttpResponse(StatusCodes.OK,
              HttpEntity(ContentType(MediaTypes.`image/jpeg`),
                HttpData.fromFile(Configs.AlternativesDir + alternative.filename)))
        }
      }
    } ~
    cache(routeCache()) {
      pathPrefix("thumbnails") {
        getFromDirectory(Configs.ThumbnailsDir)
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
