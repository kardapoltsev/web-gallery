package com.github.kardapoltsev.webgallery.util

/**
 * Created by alexey on 6/18/14.
 */
object Hardcoded {
  object ActorNames {
    val Router = "Router"
    val SessionManager = "SessionManager"
    val RequestManager = "RequestManager"
    val VKService = "VKService"
  }

  val CookieName = "wg-s-id"

  val RootUserId = 0
  val DefaultAvatarId = 0

  object VK {
    val RedirectUri = "http://localhost:9100/api/auth/callback/vk"
    val AppId = "4497069"
    val AppSecret = "iguKjSbKedfYlxvNwfig"
  }
}
