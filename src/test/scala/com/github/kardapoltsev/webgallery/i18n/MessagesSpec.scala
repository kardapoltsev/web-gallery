package com.github.kardapoltsev.webgallery.i18n


import org.scalatest.{Matchers, FlatSpec}
import spray.http.Language



/**
 * Created by alexey on 10/12/14.
 */
class MessagesSpec extends FlatSpec with Matchers {
  behavior of "Messages"


  it should "translate messages" in {
    implicit val lang = Language("ru")
    Messages("registration") should be("регистрация")
  }
  it should "return key if not found in messages" in {
    implicit val lang = Language("ru")
    Messages("someKey") should be("someKey")
  }

}
