package com.github.kardapoltsev.webgallery.util

/**
 * Created by alexey on 6/18/14.
 */
object Hardcoded {
  object ActorNames {
    val Database = "Database"
    val ImageProcessor = "ImageProcessor"
    val Router = "Router"
    val UserManager = "UserManager"
    val SessionManager = "SessionManager"
    val RequestManager = "RequestManager"
  }

  val CookieName = "wg-s-id"
  val CookieDomain = "localhost"
  val CookiePath = "/"

  object VK {
    val RedirectUri = "http://localhost:9100/api/auth/callback/vk"
    val AppId = "4497069"
    val AppSecret = "iguKjSbKedfYlxvNwfig"
  }
}
