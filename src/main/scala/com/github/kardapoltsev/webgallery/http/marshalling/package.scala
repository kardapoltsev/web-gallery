package com.github.kardapoltsev.webgallery.http


import spray.json.{DefaultJsonProtocol, JsonParser, JsonReader}
import spray.httpx.unmarshalling.{MalformedContent, Deserialized, Deserializer, FromRequestUnmarshaller}
import spray.http.HttpRequest
import com.github.kardapoltsev.webgallery.db.Tag



/**
 * Created by alexey on 6/2/14.
 */
package object marshalling extends DefaultJsonProtocol {
  import com.github.kardapoltsev.webgallery.Database._

  implicit val createTagJF = jsonFormat1(CreateTag)
  implicit val updateImageParamsJF = jsonFormat1(UpdateImageParams)
  implicit val updateImageJF = jsonFormat2(UpdateImage)

  implicit val tagUM = unmarshallerFrom(Tag.tagJF)
  implicit val createTagUM = unmarshallerFrom(createTagJF)
  implicit val updateImageParamsUM = unmarshallerFrom(updateImageParamsJF)

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
