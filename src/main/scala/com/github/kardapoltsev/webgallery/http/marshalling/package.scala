package com.github.kardapoltsev.webgallery.http


import com.github.kardapoltsev.webgallery.ImageProcessor.{TransformImageResponse, UploadImageRequest, TransformImageRequest}
import com.github.kardapoltsev.webgallery.processing.{OptionalSize, ScaleType}
import spray.httpx.SprayJsonSupport
import spray.httpx.unmarshalling._
import spray.http._
import spray.json._
import com.github.kardapoltsev.webgallery.{Configs, db, Database}
import com.github.kardapoltsev.webgallery.dto.ImageInfo
import spray.httpx.marshalling.ToResponseMarshaller
import shapeless._
import com.github.kardapoltsev.webgallery.util.Hardcoded
import com.github.kardapoltsev.webgallery.UserManager.{RegisterUserResponse, RegisterUser, GetUser, AuthResponse}



/**
 * Created by alexey on 6/2/14.
 */
package object marshalling extends DefaultJsonProtocol with SprayJsonSupport {
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


  implicit val transformImageUM: FromRequestWithParamsUnmarshaller[ImageId :: Option[Int] :: Option[Int] :: String :: HNil, TransformImageRequest] = unmarshallerFrom {
    (imageId : ImageId, width: Option[Int], height: Option[Int], scale: String) =>
      val scaleType = ScaleType.withName(scale)
      TransformImageRequest(imageId, OptionalSize(width, height, scaleType))
  }


  implicit val uploadImageUM = unmarshallerFrom {
    form: MultipartFormData =>
      val filePart = form.fields.head
      val filename = filePart.headers.find(h => h.is("content-disposition")).get.value.split("filename=").last
      UploadImageRequest(filename, filePart.entity.data.toByteArray)
  }


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


  implicit val registerUserUM: FromRequestUnmarshaller[RegisterUser] = unmarshallerFrom(RegisterUser.registerUserJF)


  implicit val updateImageUM: FromRequestWithParamsUnmarshaller[Int :: HNil, UpdateImage] =
    compositeUnmarshallerFrom {
      (body: UpdateImageParams, imageId: Int) => UpdateImage(imageId, body)
    }


  implicit val getImageUM = unmarshallerFrom {
    imageId: Int => GetImage(imageId)
  }


  implicit val getByTagUM = unmarshallerFrom {
    tag: String => GetByTag(tag)
  }


  implicit val authResponseMarshaller: ToResponseMarshaller[AuthResponse] =
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
              //              domain = Some(Hardcoded.CookieDomain),
              expires = Some(spray.http.DateTime.MaxValue)
            )
          ) :: Nil
        )
      )
    }


  implicit val registerUserResponseMarshaller: ToResponseMarshaller[RegisterUserResponse] =
    ToResponseMarshaller.of(ContentTypes.`application/json`) { (response: RegisterUserResponse, _, ctx) =>
      ctx.marshalTo(
        HttpResponse(
          StatusCodes.OK,
          response.toJson.compactPrint,
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
      ctx.marshalTo(HttpResponse(response.httpStatusCode, HttpEntity(ContentTypes.`text/plain(UTF-8)`, response.message)))
    }


  implicit def successResponseMarshaller[T <: SuccessResponse]: ToResponseMarshaller[T] =
    ToResponseMarshaller.of[T](ContentTypes.`text/plain(UTF-8)`) { (response, contentType, ctx) =>
      ctx.marshalTo(HttpResponse(response.httpStatusCode, HttpEntity(ContentTypes.`text/plain(UTF-8)`, "")))
    }


  def unmarshallerFrom[T <: ApiRequest](reader: JsonReader[T]): FromRequestUnmarshaller[T] =
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


  def unmarshallerFrom[T1, R <: ApiRequest](f: () => R): FromRequestUnmarshaller[R] =
    new Deserializer[HttpRequest, R] {
      override def apply(httpRequest: HttpRequest): Deserialized[R] = {
        Right(f().withContext(httpRequest))
      }
    }


  def unmarshallerFrom[T1, T2, T3, T4, R <: ApiRequest](f: (T1, T2, T3, T4) => R): FromRequestWithParamsUnmarshaller[T1 :: T2 :: T3 :: T4 :: HNil, R] =
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


  def compositeUnmarshallerFrom[B, T1, R <: ApiRequest](f: (B, T1) => R)(implicit u: Unmarshaller[B]): FromRequestWithParamsUnmarshaller[T1 :: HNil, R] =
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
