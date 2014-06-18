package com.github.kardapoltsev.webgallery.util

import org.mindrot.jbcrypt.BCrypt


/**
 * Created by alexey on 6/18/14.
 */
object Bcrypt {
  //scalastyle:off magic.number
  def create(password: String): String = BCrypt.hashpw(password, BCrypt.gensalt(12))
  //scalastyle:on magic.number

  def check(password: String, hash: String): Boolean = BCrypt.checkpw(password, hash)
}
