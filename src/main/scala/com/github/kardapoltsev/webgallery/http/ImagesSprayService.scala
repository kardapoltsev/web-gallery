package com.github.kardapoltsev.webgallery.http


import spray.routing.{Route, HttpService}
import scala.concurrent.{Future, ExecutionContext}
import akka.util.Timeout
import com.github.kardapoltsev.webgallery.db.Image
import java.io.{FileOutputStream, File}
import com.github.kardapoltsev.webgallery.Configs
import spray.http.{MultipartFormData, StatusCodes, HttpResponse}
import spray.json._
import com.github.kardapoltsev.webgallery.Database.{UpdateImageParams, SuccessResponse, InternalResponse, UpdateImage}



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


  val imagesRoute: Route =
    pathPrefix("images" / Segment / "original") { filename =>
      getFromFile(new File(Configs.OriginalsDir + "/" + filename))
    } ~
    cache(routeCache()) {
      pathPrefix("thumbnails") {
        getFromDirectory(Configs.ThumbnailsDir)
      }
    } ~
    pathPrefix("api") {
      //do not cache images since chunked response couldn't be cached in spray
      //see https://groups.google.com/forum/#!msg/spray-user/mL4sMr1qfwE/PyW4-CBHJlcJ
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
