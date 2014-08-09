package com.github.kardapoltsev.webgallery.http


import com.github.kardapoltsev.webgallery.db.EntityType.EntityType

import scala.concurrent.{ExecutionContext, Future}
import spray.routing._
import akka.util.Timeout
import akka.pattern.ask
import akka.actor.{Props, ActorSelection, ActorRef}
import spray.http.{HttpRequest, StatusCodes, StatusCode}
import scala.util.control.NonFatal
import scala.reflect.ClassTag
import shapeless._
import spray.httpx.unmarshalling._
import spray.httpx.marshalling.ToResponseMarshaller
import spray.http.HttpRequest
import spray.routing.UnsupportedRequestContentTypeRejection
import spray.routing.RequestContext
import spray.routing.MalformedRequestContentRejection
import shapeless.::
import spray.httpx.unmarshalling.UnsupportedContentType
import com.github.kardapoltsev.webgallery.{Server, WebGalleryActorSelection}
import com.github.kardapoltsev.webgallery.util.Hardcoded
import com.github.kardapoltsev.webgallery.db.{Session, SessionId}


/**
 * Created by alexey on 6/10/14.
 */
trait BaseSprayService { this: HttpService =>
  import BaseSprayService._

  implicit def executionContext: ExecutionContext
  implicit def requestTimeout: Timeout

  private val requestManager = actorRefFactory.actorOf(Props[RequestManager], Hardcoded.ActorNames.RequestManager)


  protected def processRequest[A](msg: ApiRequest)(implicit ct: ClassTag[A]): Result[A] = {
    (requestManager ? msg) map {
      case e: ErrorResponse => Left(e)
      case r: A => Right(r)
    } recover {
      case NonFatal(e) =>
        e.printStackTrace()
        Left(ErrorResponse.InternalServerError)
    }
  }


  def handleWith[A <: ApiRequest, B, G <: HList](extracted: G)(f: A ⇒ B)
      (implicit um: Deserializer[HttpRequest :: G, A], m: ToResponseMarshaller[B], ma: Manifest[A]): Route = {
    implicit val umm = wrap(extracted)
    new StandardRoute {
      def apply(ctx: RequestContext): Unit = {
        ctx.request.as(umm) match {
          case Right(a) => ctx.complete(f(a))
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


trait GalleryRequestContext {
  @transient var sessionId: Option[SessionId] = None

  import Hardcoded.CookieName

  def withContext(request: HttpRequest): this.type = {
    request.cookies.find(_.name == CookieName).foreach {
      cookie =>
        sessionId = Some(cookie.content.toInt)
    }
    this
  }
}


trait ApiResponse
trait ApiRequest extends GalleryRequestContext
trait AuthorizedRequest extends ApiRequest {
  @transient var session: Option[Session] = None

  def withSession(session: Session): this.type = {
    this.session = Some(session)
    this
  }
}
trait PrivilegedRequest extends AuthorizedRequest {
  @transient val subjectType: EntityType
  @transient val subjectId: Int
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

