package com.github.kardapoltsev.webgallery

import akka.actor.{ ActorLogging, Actor }
import akka.event.LoggingReceive
import com.github.kardapoltsev.webgallery.StatsManager.GetStatsResponse
import com.github.kardapoltsev.webgallery.ValidationManager.{ ValidateLoginResponse, ValidateLogin }
import com.github.kardapoltsev.webgallery.db._
import com.github.kardapoltsev.webgallery.http.{ ApiResponse, ApiRequest }
import com.github.kardapoltsev.webgallery.routing.{ ValidationManagerRequest }
import scalikejdbc.DB

/**
 * Created by koko on 24/01/15.
 */

object ValidationManager {
  case class ValidateLogin(authId: String)
    extends ApiRequest with ValidationManagerRequest

  case class ValidateLoginResponse(isValid: Boolean) extends ApiResponse
  object ValidateLoginResponse {
    implicit val _ = jsonFormat1(ValidateLoginResponse.apply)
  }
}

class ValidationManager extends Actor with ActorLogging {

  import com.github.kardapoltsev.webgallery.http.marshalling._
  import context.dispatcher

  def receive = LoggingReceive(
    Seq(validateLogin) reduceLeft (_ orElse _)
  )

  private def validateLogin: Receive = {
    case r @ ValidateLogin(authId) =>
      val isValid = DB.readOnly { implicit s =>
        Credentials.find(authId, AuthType.Direct).isEmpty
      }
      sender() ! ValidateLoginResponse(isValid)
  }
}

