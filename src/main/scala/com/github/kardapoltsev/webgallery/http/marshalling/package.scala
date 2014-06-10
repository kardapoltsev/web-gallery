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
        try {
          Right(Database.GetTags)
        } catch {
          case t: Throwable =>
            Left(MalformedContent(t.getMessage, t))
        }
      }
    }

  implicit def getTagsResponseMarshaller = marshallerFrom(getTagsResponseJF)
  implicit def createTagResponseMarshaller = marshallerFrom(createTagResponseJF)


  def marshallerFrom[A <: AnyRef](writer: JsonWriter[A]): ToResponseMarshaller[A] =
    ToResponseMarshaller.of[A](ContentTypes.`application/json`) { (response, contentType, ctx) =>
      ctx.marshalTo(HttpResponse(
        StatusCodes.OK,
        HttpEntity(ContentTypes.`application/json`, writer.write(response).compactPrint)
      ))
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
}
