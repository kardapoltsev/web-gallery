package com.github.kardapoltsev.webgallery.i18n


import java.util.{Locale, ResourceBundle}

import org.slf4j.LoggerFactory
import spray.http.Language



/**
 * Created by alexey on 10/12/14.
 */
object Messages {
  private val logger = LoggerFactory.getLogger(getClass)
  private def messages(locale: String) = ResourceBundle.getBundle("messages", new Locale(locale))
  def apply(key: String)(implicit lang: Language): String = {
    val m = messages(lang.primaryTag)
    if(m.containsKey(key)){
      m.getString(key)
    } else {
      logger.warn(s"couldn't find `$key` with lang `$lang`")
      key
    }
  }

}
