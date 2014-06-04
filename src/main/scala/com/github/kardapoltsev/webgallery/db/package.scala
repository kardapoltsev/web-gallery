package com.github.kardapoltsev.webgallery


import spray.json.{JsString, JsValue, RootJsonFormat}



/**
 * Created by alexey on 6/3/14.
 */
package object db {
  implicit val DateFormat = new RootJsonFormat[java.util.Date] {
    def read(json: JsValue): java.util.Date = new java.util.Date(new java.lang.Long(json.compactPrint))
    def write(date: java.util.Date) = JsString(date.getTime.toString)
  }
}
