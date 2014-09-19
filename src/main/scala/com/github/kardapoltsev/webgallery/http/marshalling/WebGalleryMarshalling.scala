package com.github.kardapoltsev.webgallery.http.marshalling


import com.github.kardapoltsev.webgallery.Configs
import com.github.kardapoltsev.webgallery.ImageHolder.TransformImageResponse
import com.github.kardapoltsev.webgallery.UserManager.AuthResponse
import com.github.kardapoltsev.webgallery.http.{Pagination, ApiRequest, SuccessResponse, ErrorResponse}
import com.github.kardapoltsev.webgallery.util.Hardcoded
import shapeless._
import spray.http._
import spray.httpx.SprayJsonSupport
import spray.json._
import spray.httpx.marshalling.{Marshaller, ToResponseMarshaller}
import spray.httpx.unmarshalling._

import scala.language.implicitConversions


/**
 * Created by alexey on 8/26/14.
 */
trait WebGalleryMarshalling extends SprayJsonSupport {

  implicit def jsonUnmarshallerConverter[T <: ApiRequest](reader: RootJsonReader[T]) =
    sprayJsonUnmarshaller(reader)

  implicit def jsonUnmarshaller[T <: ApiRequest : RootJsonReader]: FromRequestUnmarshaller[T] =
    new Deserializer[HttpRequest, T] {
      override def apply(httpRequest: HttpRequest): Deserialized[T] = {
        try {
          val request = jsonReader[T].read(JsonParser(httpRequest.entity.asString))
          Right(request.withContext(httpRequest))
        } catch {
          case t: Throwable =>
            Left(MalformedContent(t.getMessage, t))
        }
      }
    }


  implicit val authResponseMarshaller: ToResponseMarshaller[AuthResponse] =
    ToResponseMarshaller.of(ContentTypes.`application/json`) { (response: AuthResponse, _, ctx) =>
      ctx.marshalTo(
        HttpResponse(
          StatusCodes.Found,
          HttpEntity(ContentTypes.`application/json`, response.toJson.compactPrint),
          HttpHeaders.Location("/") ::
          HttpHeaders.`Set-Cookie`(
            HttpCookie(
              name = Hardcoded.CookieName,
              content = response.sessionId.toString,
              path = Some("/"),
              //              domain = Some(Hardcoded.CookieDomain),
              expires = Some(spray.http.DateTime.MaxValue)
            )
          ) :: Nil
        )
      )
    }


  implicit val alternativeResponseMarshaller: ToResponseMarshaller[TransformImageResponse] =
    ToResponseMarshaller.of(ContentTypes.`application/json`) { (response: TransformImageResponse, _, ctx) =>
      ctx.marshalTo(
        HttpResponse(StatusCodes.OK,
          HttpEntity(ContentType(MediaTypes.`image/jpeg`),
            HttpData.fromFile(Configs.AlternativesDir + response.alternative.filename)))
      )
    }

  implicit def errorResponseMarshaller[T <: ErrorResponse]: ToResponseMarshaller[T] =
    ToResponseMarshaller.of[T](ContentTypes.`text/plain(UTF-8)`) { (response, contentType, ctx) =>
      ctx.marshalTo(
        HttpResponse(response.httpStatusCode, HttpEntity(ContentTypes.`text/plain(UTF-8)`, response.message))
      )
    }


  implicit def successResponseMarshaller[T <: SuccessResponse]: ToResponseMarshaller[T] =
    ToResponseMarshaller.of[T](ContentTypes.`text/plain(UTF-8)`) { (response, contentType, ctx) =>
      ctx.marshalTo(HttpResponse(response.httpStatusCode, HttpEntity(ContentTypes.`text/plain(UTF-8)`, "")))
    }


  def withPagination[T <: ApiRequest with Pagination](request: T, offset: Option[Int], limit: Option[Int]): T = {
    offset.foreach(request.offset = _)
    limit.foreach(request.limit = _)
    request
  }


  def unmarshallerFrom[R <: ApiRequest](f: () => R): FromRequestUnmarshaller[R] =
    new Deserializer[HttpRequest, R] {
      override def apply(httpRequest: HttpRequest): Deserialized[R] = {
        Right(f().withContext(httpRequest))
      }
    }


  def unmarshallerFrom[T1, T2, R <: ApiRequest](f: (T1, T2) => R):
  FromRequestWithParamsUnmarshaller[T1 :: T2 :: HNil, R] =
    new Deserializer[HttpRequest :: T1 :: T2 :: HNil, R] {
      override def apply(params: HttpRequest :: T1 :: T2 :: HNil): Deserialized[R] = {
        val request :: p1 :: p2 :: HNil = params
        Right(f(p1, p2).withContext(request))
      }
    }


  def unmarshallerFrom[T1, T2, T3, R <: ApiRequest](f: (T1, T2, T3) => R):
  FromRequestWithParamsUnmarshaller[T1 :: T2 :: T3 :: HNil, R] =
    new Deserializer[HttpRequest :: T1 :: T2 :: T3 :: HNil, R] {
      override def apply(params: HttpRequest :: T1 :: T2 :: T3 :: HNil): Deserialized[R] = {
        val request :: p1 :: p2 :: p3 :: HNil = params
        Right(f(p1, p2, p3).withContext(request))
      }
    }


  def unmarshallerFrom[T1, T2, T3, T4, R <: ApiRequest](f: (T1, T2, T3, T4) => R):
  FromRequestWithParamsUnmarshaller[T1 :: T2 :: T3 :: T4 :: HNil, R] =
    new Deserializer[HttpRequest :: T1 :: T2 :: T3 :: T4 :: HNil, R] {
      override def apply(params: HttpRequest :: T1 :: T2 :: T3 :: T4 :: HNil): Deserialized[R] = {
        val request :: p1 :: p2 :: p3 :: p4 :: HNil = params
        Right(f(p1, p2, p3, p4).withContext(request))
      }
    }


  type FromRequestWithParamsUnmarshaller[K <: HList, T] = Deserializer[HttpRequest :: K, T]


  def unmarshallerFrom[T1, R <: ApiRequest](f: (T1) => R): FromRequestWithParamsUnmarshaller[T1 :: HNil, R] =
    new Deserializer[HttpRequest :: T1 :: HNil, R] {
      override def apply(params: HttpRequest :: T1 :: HNil): Deserialized[R] = {
        val request :: p1 :: HNil = params
        Right(f(p1).withContext(request))
      }
    }


  def compositeUnmarshallerFrom[B, T1, R <: ApiRequest](f: (B, T1) => R)(implicit u: Unmarshaller[B]):
  FromRequestWithParamsUnmarshaller[T1 :: HNil, R] =
    new Deserializer[HttpRequest :: T1 :: HNil, R] {
      override def apply(params: HttpRequest :: T1 :: HNil): Deserialized[R] = {
        val request :: p1 :: HNil = params

        request.entity.as[B] match {
          case Right(body) => Right(f(body, p1).withContext(request))
          case Left(x) => Left(x)
        }

      }
    }


  def objectUM[A <: ApiRequest](o: A): FromRequestUnmarshaller[A] =
    new Deserializer[HttpRequest, A] {
      override def apply(httpRequest: HttpRequest): Deserialized[A] = {
        Right(o.withContext(httpRequest))
      }
    }
}
