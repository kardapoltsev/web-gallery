package com.github.kardapoltsev.webgallery.http


import spray.httpx.unmarshalling.{MalformedContent, Deserialized, Deserializer, FromRequestUnmarshaller}
import spray.http._
import spray.json._
import com.github.kardapoltsev.webgallery.db.gen
import com.github.kardapoltsev.webgallery.dto.ImageInfo
import spray.httpx.marshalling.ToResponseMarshaller
import spray.http.HttpRequest
import spray.http.HttpResponse
import com.github.kardapoltsev.webgallery.Database
import shapeless._


/**
 * Created by alexey on 6/2/14.
 */
package object marshalling extends DefaultJsonProtocol {
  import com.github.kardapoltsev.webgallery.Database._

  implicit val createTagJF = jsonFormat1(CreateTag)
  implicit val updateImageParamsJF = jsonFormat1(UpdateImageParams)
  implicit val updateImageJF = jsonFormat2(UpdateImage)

  implicit val imageInfoJF = jsonFormat4(ImageInfo.apply)

  implicit val tagJF = jsonFormat2(gen.Tag.apply)
  implicit val getTagsResponseJF = jsonFormat1(GetTagsResponse.apply)
  implicit val createTagResponseJF = jsonFormat1(CreateTagResponse.apply)

  implicit val createTagUM = unmarshallerFrom(createTagJF)
  implicit val updateImageParamsUM = unmarshallerFrom(updateImageParamsJF)
  implicit val tagUM = unmarshallerFrom(tagJF)

  implicit val getTagsUM: FromRequestUnmarshaller[GetTags.type] =
    new Deserializer[HttpRequest, GetTags.type] {
      override def apply(httpRequest: HttpRequest): Deserialized[GetTags.type] = {
        Right(Database.GetTags)
      }
    }

  implicit val searchTagsUM = unmarshallerFrom {
    query: String => Database.SearchTags(query)
  }




  implicit def errorResponseMarshaller[T <: ErrorResponse]: ToResponseMarshaller[T] =
    ToResponseMarshaller.of[T](ContentTypes.`text/plain(UTF-8)`) { (response, contentType, ctx) =>
      ctx.marshalTo(HttpResponse(response.httpStatusCode, HttpEntity(ContentTypes.`text/plain(UTF-8)`, response.message)))
    }


  implicit def successResponseMarshaller[T <: SuccessResponse]: ToResponseMarshaller[T] =
    ToResponseMarshaller.of[T](ContentTypes.`text/plain(UTF-8)`) { (response, contentType, ctx) =>
      ctx.marshalTo(HttpResponse(response.httpStatusCode, HttpEntity(ContentTypes.`text/plain(UTF-8)`, "")))
    }


  def unmarshallerFrom[T <: AnyRef](reader: JsonReader[T]): FromRequestUnmarshaller[T] =
    new Deserializer[HttpRequest, T] {
      override def apply(httpRequest: HttpRequest): Deserialized[T] = {
        try {
          val request = reader.read(JsonParser(httpRequest.entity.asString))
          Right(request)
        } catch {
          case t: Throwable =>
            Left(MalformedContent(t.getMessage, t))
        }
      }
    }


  def unmarshallerFrom[T1, R <: InternalRequest](f: () => R): FromRequestUnmarshaller[R] =
    new Deserializer[HttpRequest, R] {
      override def apply(request: HttpRequest): Deserialized[R] = {
        Right(f())
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

}
