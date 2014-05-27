package com.github.kardapoltsev.webgallery.http

import spray.routing.{Route, HttpService}
import akka.actor._
import spray.http._
import spray.can.Http
import spray.httpx.unmarshalling._
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import com.github.kardapoltsev.webgallery.{Database, Configs}
import com.github.kardapoltsev.webgallery.Database.{GetTagsResponse, GetFilesResponse}
import java.io.{FileOutputStream, ByteArrayInputStream}


/**
 * Created by alexey on 5/5/14.
 */
class RequestDispatcher extends Actor with HttpService with ActorLogging {


  def actorRefFactory: ActorContext = context

  val database = context.actorSelection("/user/Database")

  def receive: Receive = serviceMessage orElse runRoute(route)


  import concurrent.duration._
  import context.dispatcher
  import spray.json._
  import spray.routing.directives.CachingDirectives._

  implicit val requestTimeout = Timeout(3.seconds)


  val route: Route =
    respondWithMediaType(MediaTypes.`text/html`) {
      path("") {
        complete{
          database ? Database.GetTags map {
            case GetTagsResponse(tags) =>
              html.index(tags).toString
          }
        }
      }
    } ~
    pathPrefix("assets") {
      getFromDirectory("/home/alexey/projects/other/web-gallery/assets/")
    } ~
      pathPrefix("images") {
        getFromDirectory(Configs.OriginalsDir)
      } ~
      //do not cache images since chunked response couldn't be cached in spray
      //see https://groups.google.com/forum/#!msg/spray-user/mL4sMr1qfwE/PyW4-CBHJlcJ
      cache(routeCache()) {
        pathPrefix("thumbnails") {
          getFromDirectory(Configs.ThumbnailsDir)
        }
    } ~
    respondWithMediaType(MediaTypes.`application/json`){
      path("albums" / Segment) { case album =>
        complete{
          database ? Database.GetByAlbum(album) map {
            case GetFilesResponse(images) => images.toJson.compactPrint
          }
        }
      }
    } ~
    path("upload") {
      post {
        entity(as[MultipartFormData]) { formData =>
              log.debug(formData.toString)
              val filePart = formData.fields.head
              val fileName = filePart.headers.find(h => h.is("content-disposition")).get.value.split("filename=").last
              saveAttachment(fileName, filePart.entity.data.toByteArray)
              redirect("/", StatusCodes.MovedPermanently)
          }
      }
    }


  private def saveAttachment(filename: String, content: Array[Byte]): Unit = {
    val fos = new FileOutputStream(Configs.UnprocessedDir + "/" + filename)
    try {
      fos.write(content)
    } finally {
      fos.close()
    }
  }



  import java.io.File
  protected def getFileNamesFrom(dir: String): Seq[String] = {
    getNamesFrom(dir, {f: File => f.isFile})
  }
  protected def getDirectoryNamesFrom(dir: String): Seq[String] = {
    getNamesFrom(dir, {f: File => f.isDirectory})
  }
  private def getNamesFrom(dir: String, filter: File => Boolean): Seq[String] = {
    new File(dir).listFiles().filter(filter).map(_.getName).toSeq
  }


  def serviceMessage: Receive = {
    case Http.Bound(address) =>
      log.info(s"RequestDispatcher successfully bound to $address")
  }
}

