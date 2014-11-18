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
    val EventListener = "EventListener"
  }

  val CookieName = "wg-s-id"

  val RootUserId = 1
  val AnonymousUserId = 2
  val DefaultAvatarId = 1
  val DefaultCoverId = 2

  object VK {
    val RedirectUri = "http://fotik.me/api/auth/callback/vk"
    val AppId = "4497069"
    val AppSecret = "iguKjSbKedfYlxvNwfig"
  }

  object Tags {
    val Untagged = "untagged"
    val All = "all"
  }

}
