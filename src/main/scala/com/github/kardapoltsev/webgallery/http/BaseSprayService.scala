package com.github.kardapoltsev.webgallery.http

import akka.event.LoggingReceive
import com.github.kardapoltsev.webgallery.acl.Permissions
import com.github.kardapoltsev.webgallery.db.EntityType.EntityType
import org.slf4j.LoggerFactory

import scala.concurrent.{ ExecutionContext, Future }
import spray.routing._
import akka.util.Timeout
import akka.pattern.ask
import akka.actor._
import spray.http._
import scala.util.control.NonFatal
import scala.reflect.ClassTag
import shapeless._
import spray.httpx.unmarshalling._
import spray.httpx.marshalling.ToResponseMarshaller
import spray.routing.UnsupportedRequestContentTypeRejection
import spray.routing.RequestContext
import spray.routing.MalformedRequestContentRejection
import spray.httpx.unmarshalling.UnsupportedContentType
import com.github.kardapoltsev.webgallery.{ Server, WebGalleryActorSelection }
import com.github.kardapoltsev.webgallery.util.Hardcoded
import com.github.kardapoltsev.webgallery.db.{ Session, SessionId }

/**
 * Created by alexey on 6/10/14.
 */
trait BaseSprayService { this: HttpService =>
  import BaseSprayService._
  import concurrent.duration._

  implicit def executionContext: ExecutionContext
  implicit def requestTimeout: Timeout

  private lazy val requestManager = actorRefFactory.actorOf(Props[RequestManager], Hardcoded.ActorNames.RequestManager)
  protected lazy val router = WebGalleryActorSelection.routerSelection
  val offsetLimit = parameters('offset.as[Int].?, 'limit.as[Int].?)

  class RequestHandler[A <: ApiRequest, B <: ApiResponse](msg: A, target: ActorRef, r: RequestContext)(implicit m: ToResponseMarshaller[B], ct: ClassTag[B]) extends Actor with ActorLogging {
    import StatusCodes._
    import language.implicitConversions
    import com.github.kardapoltsev.webgallery.http.marshalling.WebGalleryMarshalling.errorResponseMarshaller

    context.setReceiveTimeout(5.seconds)
    target ! msg

    def receive = LoggingReceive {
      case response: B => complete(response)
      case error: ErrorResponse => complete(error)
      case ReceiveTimeout => complete(GatewayTimeout)
    }

    private def complete[B](response: B)(implicit m: ToResponseMarshaller[B]): Unit = {
      r.complete(response)
      context.stop(self)
    }
  }

  //this class is workaround for scala type inference and requests that have both body and query params
  case class HandlerWrapper[A <: ApiResponse](msg: ApiRequest)(implicit m: ToResponseMarshaller[A], ct: ClassTag[A]) {
    def props(target: ActorRef, r: RequestContext): Props =
      Props(new RequestHandler(msg, target, r))
  }

  def perRequest[A <: ApiRequest, B <: ApiResponse, G <: HList](extracted: G)(f: A => HandlerWrapper[B])(implicit um: Deserializer[HttpRequest :: G, A], m: ToResponseMarshaller[B], ma: Manifest[A], ct: ClassTag[B]): Route = {
    implicit val umm: FromRequestUnmarshaller[A] = wrap[A, G](extracted)
    perRequestRoute[A](f)
  }

  protected def perRequest[A <: ApiRequest, B <: ApiResponse](implicit um: Deserializer[HttpRequest, A], m: ToResponseMarshaller[B], ma: Manifest[A], ct: ClassTag[B]): Route = {
    perRequestRoute[A] { a: A => HandlerWrapper[B](a) }
  }

  private def perRequestRoute[A <: ApiRequest](f: A => HandlerWrapper[_])(implicit um: Deserializer[HttpRequest, A]): Route = {
    new StandardRoute {
      def apply(ctx: RequestContext): Unit = {
        ctx.request.as(um) match {
          case Right(a) =>
            actorRefFactory.actorOf(f(a).props(requestManager, ctx))
          case Left(UnsupportedContentType(supported)) => reject(UnsupportedRequestContentTypeRejection(supported))
          case Left(MalformedContent(errorMsg, cause)) => reject(MalformedRequestContentRejection(errorMsg, cause))
          case Left(ContentExpected) => reject(RequestEntityExpectedRejection)
        }
      }
    }
  }

  @deprecated()
  protected def processRequest[A](msg: ApiRequest): Unit = {
    requestManager ! msg
  }

  @deprecated()
  def handleRequest[A <: ApiRequest, B](f: A ⇒ B)(implicit um: FromRequestUnmarshaller[A]): Route = {
    new StandardRoute {
      def apply(ctx: RequestContext): Unit = {
        ctx.request.as(um) match {
          case Right(a) => f(a.withContext(ctx))
          case Left(UnsupportedContentType(supported)) => reject(UnsupportedRequestContentTypeRejection(supported))
          case Left(MalformedContent(errorMsg, cause)) => reject(MalformedRequestContentRejection(errorMsg, cause))
          case Left(ContentExpected) => reject(RequestEntityExpectedRejection)
        }
      }
    }
  }
  @deprecated()
  def handleRequest[A <: ApiRequest, B, G <: HList](extracted: G)(f: A ⇒ B)(implicit um: Deserializer[HttpRequest :: G, A], ma: Manifest[A]): Route = {
    implicit val umm = wrap(extracted)
    new StandardRoute {
      def apply(ctx: RequestContext): Unit = {
        ctx.request.as(umm) match {
          case Right(a) => f(a.withContext(ctx))
          case Left(UnsupportedContentType(supported)) => reject(UnsupportedRequestContentTypeRejection(supported))
          case Left(MalformedContent(errorMsg, cause)) => reject(MalformedRequestContentRejection(errorMsg, cause))
          case Left(ContentExpected) => reject(RequestEntityExpectedRejection)
        }
      }
    }
  }

  private def wrap[A, G <: HList](extracted: G)(implicit um: Deserializer[HttpRequest :: G, A]): FromRequestUnmarshaller[A] =
    new Deserializer[HttpRequest, A] {
      override def apply(httpRequest: HttpRequest): Deserialized[A] = {
        um(::(httpRequest, extracted))
      }
    }

}

object BaseSprayService {
  type Result[T] = Future[Either[ErrorResponse, T]]
}

trait Pagination {
  @transient var offset = 0
  @transient var limit = 20

  def withPagination(o: Option[Int], l: Option[Int]): this.type = {
    o.foreach(offset = _)
    l.foreach(limit = _)
    this
  }

}

trait GalleryRequestContext {
  @transient var sessionId: Option[SessionId] = None
  @transient var ctx: Option[RequestContext] = None

  import Hardcoded.CookieName

  def withContext(ctx: RequestContext): this.type = {
    this.ctx = Some(ctx)
    this
  }

  def withRequest(request: HttpRequest): this.type = {
    request.cookies.find(_.name == CookieName).foreach {
      cookie =>
        sessionId = Some(cookie.content)
    }
    this
  }
}

trait ApiResponse
trait ApiRequest extends GalleryRequestContext {
  @transient var session: Option[Session] = None
  @transient protected val logger = LoggerFactory.getLogger(getClass)

  def withSession(session: Session): this.type = {
    this.session = Some(session)
    this
  }

  def requesterId = session.get.userId

  def userAgent: Option[String] = ctx.flatMap(_.request.header[HttpHeaders.`User-Agent`].map(h => h.value))

}

trait AuthorizedRequest extends ApiRequest

/**
 * Requests that require `Write` permissions should be marked with this trait
 * `Read` access is handled directly in sql select queries
 */
trait PrivilegedRequest extends AuthorizedRequest {
  def permissions: Permissions.Permission
  def subjectType: EntityType
  def subjectId: Int
}

trait ValidatedRequest extends ApiRequest {
  def validate: Boolean
}

sealed trait TextResponse extends ApiResponse with Serializable {
  def httpStatusCode: StatusCode
}

sealed abstract class SuccessResponse extends TextResponse {
  override def httpStatusCode: StatusCode = StatusCodes.OK
}

case object SuccessResponse extends SuccessResponse

sealed abstract class ErrorResponse(override val httpStatusCode: StatusCode, val message: String = "")
  extends TextResponse

object ErrorResponse {
  object Unauthorized extends ErrorResponse(StatusCodes.Unauthorized)
  object AuthenticationTimeout extends ErrorResponse(StatusCodes.registerCustom(419, "Session expired"))
  object NotFound extends ErrorResponse(StatusCodes.NotFound)
  object BadRequest extends ErrorResponse(StatusCodes.BadRequest)
  object Forbidden extends ErrorResponse(StatusCodes.Forbidden)
  object Locked extends ErrorResponse(StatusCodes.Locked)
  object Conflict extends ErrorResponse(StatusCodes.Conflict)
  object UnprocessableEntity extends ErrorResponse(StatusCodes.UnprocessableEntity)
  object TooManyRequests extends ErrorResponse(StatusCodes.TooManyRequests)
  object Processing extends ErrorResponse(StatusCodes.Processing)
  object InternalServerError extends ErrorResponse(StatusCodes.InternalServerError)
  object ServiceUnavailable extends ErrorResponse(StatusCodes.ServiceUnavailable)

  case class Custom(override val httpStatusCode: StatusCode, override val message: String) extends ErrorResponse(httpStatusCode)
}

