package com.github.kardapoltsev.webgallery.oauth

import spray.json.DefaultJsonProtocol

/**
 * Created by alexey on 8/8/14.
 */
object VK extends DefaultJsonProtocol {

  case class UserInfo(uid: Long, first_name: String, last_name: String, photo_max_orig: Option[String])
  object UserInfo {
    implicit val _ = jsonFormat4(UserInfo.apply)
  }

  case class GetUserInfoResponse(response: Seq[UserInfo])
  object GetUserInfoResponse {
    implicit val _ = jsonFormat1(GetUserInfoResponse.apply)
  }


  case class GetTokenResponse(access_token: String, expires_in: Long, user_id: Long)
  object GetTokenResponse {
    implicit val _ = jsonFormat3(GetTokenResponse.apply)
  }

}
