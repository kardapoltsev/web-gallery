package self.edu.server.http

import spray.routing.{RequestContext, Route, HttpService}
import akka.actor._
import spray.http._
import spray.can.Http
import spray.httpx.unmarshalling._
import akka.pattern.{ask, pipe}
import spray.http.HttpResponse
import akka.util.Timeout
import spray.routing.RequestContext
import akka.actor.SupervisorStrategy.Stop
import scala.concurrent.Future
import spray.json.{JsonParser, JsonReader, DefaultJsonProtocol}
import com.typesafe.config.ConfigFactory
import self.edu.server.{Image, Database, Configs}


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
          database ? Database.GetAlbums map {
            case dates: Seq[String] =>
              html.index(dates).toString
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
            case images: Seq[Image] => images.toJson.compactPrint
          }
        }
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

