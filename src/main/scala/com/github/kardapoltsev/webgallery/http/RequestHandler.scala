package com.github.kardapoltsev.webgallery.http

import akka.actor._
import akka.event.LoggingReceive
import com.github.kardapoltsev.webgallery.SessionManager.{ ObtainSessionResponse, ObtainSession }
import com.github.kardapoltsev.webgallery.{Configs, WebGalleryActorSelection}
import com.github.kardapoltsev.webgallery.acl.Permissions
import com.github.kardapoltsev.webgallery.db._
import com.github.kardapoltsev.webgallery.util.Hardcoded
import spray.http.StatusCodes
import spray.httpx.marshalling.ToResponseMarshaller
import spray.routing.RequestContext

import scala.reflect.ClassTag

/**
 * Created by alexey on 4/3/15.
 */
class RequestHandler[A <: ApiRequest, B <: ApiResponse](msg: A, r: RequestContext)(implicit m: ToResponseMarshaller[B], ct: ClassTag[B]) extends Actor with ActorLogging {
  import StatusCodes._
  import language.implicitConversions
  import concurrent.duration._
  import com.github.kardapoltsev.webgallery.http.marshalling.WebGalleryMarshalling.errorResponseMarshaller

  private def router = WebGalleryActorSelection.routerSelection
  private def sessionManager = WebGalleryActorSelection.sessionManagerSelection

  context.setReceiveTimeout(Configs.RequestTimeout)

  //TODO: think about validation in marshalling
  msg match {
    case request: ValidatedRequest if !request.validate =>
      complete(ErrorResponse.BadRequest)
    case request =>
      sessionManager ! ObtainSession(msg.sessionId, msg.userAgent)
  }

  def receive = obtainingSession

  def waitingResponse = LoggingReceive {
    case response: B => complete(response)
    case error: ErrorResponse => complete(error)
    case ReceiveTimeout => complete(GatewayTimeout)
  }

  def obtainingSession: Receive = {
    case ObtainSessionResponse(session) =>
      msg match {
        case privileged: PrivilegedRequest =>
          if (isAccessGranted(privileged, session.userId)) {
            router ! privileged.withSession(session)
          } else {
            self ! ErrorResponse.Forbidden
          }
        case authorized: AuthorizedRequest =>
          session.userId match {
            case Hardcoded.AnonymousUserId =>
              self ! ErrorResponse.Unauthorized
            case _ =>
              router ! authorized.withSession(session)
          }
        case _ =>
          router ! msg.withSession(session)
      }
      context become waitingResponse

    case ReceiveTimeout => complete(GatewayTimeout)
  }

  private def isAccessGranted(r: PrivilegedRequest, requesterId: UserId): Boolean = {
    r.permissions match {
      case Permissions.Write =>
        r.subjectType match {
          case EntityType.Tag => Tag.find(r.subjectId).fold(false)(_.ownerId == requesterId)
          case EntityType.Image => Image.find(r.subjectId).fold(false)(_.ownerId == requesterId)
          case EntityType.Comment => Comment.find(r.subjectId).fold(false)(_.ownerId == requesterId)
        }
      case Permissions.Read =>
        r.subjectType match {
          case EntityType.Tag => Acl.existsForTag(r.subjectId, requesterId)
          case EntityType.Image => Acl.existsForImage(r.subjectId, requesterId)
        }
    }
  }

  private def complete[C](response: C)(implicit m: ToResponseMarshaller[C]): Unit = {
    r.complete(response)
    context.stop(self)
  }

}

//this class is workaround for scala type inference and requests that have both body and query params
case class HandlerWrapper[A <: ApiResponse](msg: ApiRequest)(implicit m: ToResponseMarshaller[A], ct: ClassTag[A]) {
  def props(r: RequestContext): Props =
    Props(new RequestHandler(msg, r))
}
