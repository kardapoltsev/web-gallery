package com.github.kardapoltsev.webgallery.http


import spray.httpx.unmarshalling._
import spray.http._
import spray.json._
import com.github.kardapoltsev.webgallery.db.gen
import com.github.kardapoltsev.webgallery.dto.ImageInfo
import spray.httpx.marshalling.ToResponseMarshaller
import com.github.kardapoltsev.webgallery.Database
import shapeless._
import com.github.kardapoltsev.webgallery.util.Hardcoded
import com.github.kardapoltsev.webgallery.UserManager.{GetUser, AuthResponse}



/**
 * Created by alexey on 6/2/14.
 */
package object marshalling extends DefaultJsonProtocol {
  import com.github.kardapoltsev.webgallery.Database._
  import spray.httpx.SprayJsonSupport._
  import spray.httpx.marshalling._
  import com.github.kardapoltsev.webgallery.db._

  implicit val createTagJF = jsonFormat1(CreateTag)
  implicit val updateImageParamsJF = jsonFormat1(UpdateImageParams)
  implicit val updateImageJF = jsonFormat2(UpdateImage)

  implicit val imageInfoJF = jsonFormat4(ImageInfo.apply)

  implicit val getTagsResponseJF = jsonFormat1(GetTagsResponse.apply)
  implicit val createTagResponseJF = jsonFormat1(CreateTagResponse.apply)

  implicit val createTagUM = unmarshallerFrom(createTagJF)


  implicit val getTagsUM: FromRequestUnmarshaller[GetTags.type] =
    new Deserializer[HttpRequest, GetTags.type] {
      override def apply(httpRequest: HttpRequest): Deserialized[GetTags.type] = {
        Right(Database.GetTags)
      }
    }

  implicit val searchTagsUM = unmarshallerFrom {
    query: String => Database.SearchTags(query)
  }


  implicit val getUserUM = unmarshallerFrom {
    userId: UserId => GetUser(userId)
  }


  implicit val updateImageUM: FromRequestWithParamsUnmarshaller[Int :: HNil, UpdateImage] =
    compositeUnmarshallerFrom {
      (body: UpdateImageParams, imageId: Int) => UpdateImage(imageId, body)
    }

  implicit val getImageUM = unmarshallerFrom {
    imageId: Int => GetImage(imageId)
  }

  implicit val authResponseMarshaller =
    ToResponseMarshaller.of(ContentTypes.`application/json`) { (response: AuthResponse, _, ctx) =>
      ctx.marshalTo(
        HttpResponse(
          StatusCodes.OK,
          response.toJson.compactPrint,
          HttpHeaders.`Set-Cookie`(
            HttpCookie(
              name = Hardcoded.CookieName,
              content = response.sessionId.toString,
              path = Some("/"),
              domain = Some(Hardcoded.CookieDomain),
              expires = Some(spray.http.DateTime.MaxValue)
            )
          ) :: Nil
        )
      )
    }

  implicit def errorResponseMarshaller[T <: ErrorResponse]: ToResponseMarshaller[T] =
    ToResponseMarshaller.of[T](ContentTypes.`text/plain(UTF-8)`) { (response, contentType, ctx) =>
      ctx.marshalTo(HttpResponse(response.httpStatusCode, HttpEntity(ContentTypes.`text/plain(UTF-8)`, response.message)))
    }


  implicit def successResponseMarshaller[T <: SuccessResponse]: ToResponseMarshaller[T] =
    ToResponseMarshaller.of[T](ContentTypes.`text/plain(UTF-8)`) { (response, contentType, ctx) =>
      ctx.marshalTo(HttpResponse(response.httpStatusCode, HttpEntity(ContentTypes.`text/plain(UTF-8)`, "")))
    }


  def unmarshallerFrom[T <: InternalRequest](reader: JsonReader[T]): FromRequestUnmarshaller[T] =
    new Deserializer[HttpRequest, T] {
      override def apply(httpRequest: HttpRequest): Deserialized[T] = {
        try {
          val request = reader.read(JsonParser(httpRequest.entity.asString))
          Right(request.withContext(httpRequest))
        } catch {
          case t: Throwable =>
            Left(MalformedContent(t.getMessage, t))
        }
      }
    }


  def unmarshallerFrom[T1, R <: InternalRequest](f: () => R): FromRequestUnmarshaller[R] =
    new Deserializer[HttpRequest, R] {
      override def apply(httpRequest: HttpRequest): Deserialized[R] = {
        Right(f().withContext(httpRequest))
      }
    }


  type FromRequestWithParamsUnmarshaller[K <: HList, T] = Deserializer[HttpRequest :: K, T]


  def unmarshallerFrom[T1, R <: InternalRequest](f: (T1) => R): FromRequestWithParamsUnmarshaller[T1 :: HNil, R] =
    new Deserializer[HttpRequest :: T1 :: HNil, R] {
      override def apply(params: HttpRequest :: T1 :: HNil): Deserialized[R] = {
        val request :: p1 :: HNil = params
        Right(f(p1))
      }
    }


  def compositeUnmarshallerFrom[B, T1, R <: InternalRequest](f: (B, T1) => R)(implicit u: Unmarshaller[B]): FromRequestWithParamsUnmarshaller[T1 :: HNil, R] =
    new Deserializer[HttpRequest :: T1 :: HNil, R] {
      override def apply(params: HttpRequest :: T1 :: HNil): Deserialized[R] = {
        val request :: p1 :: HNil = params

        request.entity.as[B] match {
          case Right(body) => Right(f(body, p1))
          case Left(x) => Left(x)
        }

      }
    }
}
