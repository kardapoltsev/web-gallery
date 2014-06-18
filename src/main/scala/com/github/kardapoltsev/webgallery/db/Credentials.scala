package com.github.kardapoltsev.webgallery.db


import com.github.kardapoltsev.webgallery.db.AuthType.AuthType
import scalikejdbc._
import spray.json.{JsValue, JsString, JsonFormat}
import com.sun.xml.internal.ws.encoding.soap.DeserializationException


/**
 * Created by alexey on 6/17/14.
 */
object Credentials {
  import gen.Credentials._

  def find(authId: String, authType: AuthType)(implicit session: DBSession = autoSession): Option[Credentials] = {
    withSQL {
      select.from(Credentials as c)
          .where(sqls.toAndConditionOpt(
            Some(sqls.eq(c.authId, authId)),
            Some(sqls.eq(c.authType, authType.toString))
          ))
    }.map(Credentials(c.resultName)).single.apply()
  }

}


object AuthType extends Enumeration {
  type AuthType = Value
  val Direct = Value("Direct")


  implicit object AuthTypeJF extends JsonFormat[AuthType.Value] {
    override def read(json: JsValue): AuthType.Value = json match {
      case JsString(s) => AuthType.withName(s)
      case _ => throw new DeserializationException("AuthType enum string expected")
    }
    override def write(obj: AuthType.Value): JsValue = JsString(obj.toString)
  }

}