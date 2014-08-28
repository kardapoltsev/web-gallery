package com.github.kardapoltsev.webgallery.oauth


import akka.actor.{ActorLogging, Actor}
import akka.event.LoggingReceive
import com.github.kardapoltsev.webgallery.util.Hardcoded
import spray.http.{Uri, HttpMethods, HttpRequest}
import spray.client.pipelining._
import spray.httpx.SprayJsonSupport._
import akka.pattern.pipe
import spray.json.DefaultJsonProtocol
import scala.concurrent.Future



/**
 * Created by alexey on 8/28/14.
 */
object VKService extends DefaultJsonProtocol {
  case class GetUserInfo(userId: String, fields: Seq[String] = Seq.empty)
  case class GetToken(code: String)

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


class VKService extends Actor with ActorLogging {
  import VKService._
  import context.dispatcher
  import com.github.kardapoltsev.webgallery.Configs.Timeouts.Background


  val getVKAuthToken: HttpRequest => Future[GetTokenResponse] = sendReceive ~> unmarshal[GetTokenResponse]
  val getVKUserInfo: HttpRequest => Future[GetUserInfoResponse] = sendReceive ~> unmarshal[GetUserInfoResponse]

  def receive: Receive = LoggingReceive(processGetToken orElse processGetUserInfo)


  private def processGetToken: Receive = {
    case GetToken(code) =>
      val getToken = HttpRequest(HttpMethods.GET, Uri("https://oauth.vk.com/access_token").withQuery(
        "client_id" -> Hardcoded.VK.AppId,
        "client_secret"  -> Hardcoded.VK.AppSecret,
        "code" -> code,
        "redirect_uri" -> Hardcoded.VK.RedirectUri
      ))
      getVKAuthToken(getToken) pipeTo sender
  }


  private def processGetUserInfo: Receive = {
    case GetUserInfo(userId, fields) =>
      getVKUserInfo(getUserInfoRequest(userId, fields)) pipeTo sender()
  }


  private def getUserInfoRequest(userId: String, fields: Seq[String]) =
    HttpRequest(HttpMethods.GET, Uri("https://api.vkontakte.ru/method/users.get.json").
        withQuery(
          "uids" -> userId,
          "fields" -> fields.mkString(",")
        )
    )

}
