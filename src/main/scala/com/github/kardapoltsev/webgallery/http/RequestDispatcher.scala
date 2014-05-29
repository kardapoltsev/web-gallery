package com.github.kardapoltsev.webgallery.http

import spray.routing.{Route, HttpService}
import akka.actor._
import spray.http._
import spray.can.Http
import spray.httpx.unmarshalling._
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import com.github.kardapoltsev.webgallery.{WebGalleryActorSelection, Database, Configs}
import com.github.kardapoltsev.webgallery.Database.{GetTagsResponse, GetFilesResponse}
import java.io.{FileOutputStream, ByteArrayInputStream}
import concurrent.{ExecutionContext, Future}
import com.github.kardapoltsev.webgallery.db.Image



/**
 * Created by alexey on 5/5/14.
 */
class RequestDispatcher extends Actor with HttpService with ActorLogging
  with ImagesSprayService with WebGalleryActorSelection {

  def actorRefFactory: ActorContext = context

  def receive: Receive = serviceMessage orElse runRoute(imagesRoute)


  import concurrent.duration._
  override implicit val executionContext = context.dispatcher
  override implicit val requestTimeout = Timeout(FiniteDuration(3, concurrent.duration.SECONDS))


  override protected def getTags: Future[Seq[String]] = {
    (databaseSelection ? Database.GetTags).map {
      case GetTagsResponse(tags) => tags.map(_.name)
    }
  }


  override protected def getByAlbum(tag: String): Future[Seq[Image]] = {
    (databaseSelection ? Database.GetByTag(tag)).map {
      case GetFilesResponse(images) => images
    }
  }


  def serviceMessage: Receive = {
    case Http.Bound(address) =>
      log.info(s"RequestDispatcher successfully bound to $address")
  }
}


trait ImagesSprayService { this: HttpService =>

  import spray.routing.directives.CachingDirectives._
  implicit def executionContext: ExecutionContext
  implicit def requestTimeout: Timeout
  val cwd = System.getProperty("user.dir")

  
  val imagesRoute: Route =
    respondWithMediaType(MediaTypes.`text/html`) {
      path("") {
        complete{
          getTags map {
            case tags =>
              html.index(tags, Seq.empty).toString
          }
        }
      }
    } ~
        pathPrefix("assets") {
          getFromDirectory(cwd + "/assets")
        } ~
        //do not cache images since chunked response couldn't be cached in spray
        //see https://groups.google.com/forum/#!msg/spray-user/mL4sMr1qfwE/PyW4-CBHJlcJ
        pathPrefix("images") {
          getFromDirectory(Configs.OriginalsDir)
        } ~
        cache(routeCache()) {
          pathPrefix("thumbnails") {
            getFromDirectory(Configs.ThumbnailsDir)
          }
        } ~
        respondWithMediaType(MediaTypes.`text/html`){
          path("albums" / Segment) { case album =>
            complete {
              getTags zip getByAlbum(album) map {
                case (tags, images) => html.index(tags, images).toString
              }
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
  
  
  protected def getTags: Future[Seq[String]]
  
  
  protected def getByAlbum(album: String): Future[Seq[Image]]
  
  
  protected def saveAttachment(filename: String, content: Array[Byte]): Unit = {
    val fos = new FileOutputStream(Configs.UnprocessedDir + "/" + filename)
    try {
      fos.write(content)
    } finally {
      fos.close()
    }
  }
}
