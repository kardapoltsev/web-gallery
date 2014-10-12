package com.github.kardapoltsev.webgallery.util


import java.util.UUID



/**
 * Created by alexey on 10/12/14.
 */
object IdGenerator {
  def nextSessionId = UUID.randomUUID().toString
}
