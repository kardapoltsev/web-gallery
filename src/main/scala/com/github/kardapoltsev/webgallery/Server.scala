package com.github.kardapoltsev.webgallery

import akka.actor.{ Props, ActorSystem }
import com.github.kardapoltsev.webgallery.db.Database
import com.github.kardapoltsev.webgallery.http.RequestDispatcher
import akka.io.IO
import org.joda.time.DateTimeZone
import spray.can.Http
import java.io.File
import com.github.kardapoltsev.webgallery.routing.Router

/**
 * Created by alexey on 5/5/14.
 */
object Server {
  import com.github.kardapoltsev.webgallery.util.Hardcoded.ActorNames
  import Configs.config

  def main(args: Array[String]): Unit = {
    implicit val system = createActorSystem
    init()
    bind()
  }

  def createActorSystem: ActorSystem = ActorSystem("WebGallery", config)

  def init()(implicit system: ActorSystem): Unit = {
    mkdirs()
    DateTimeZone.setDefault(DateTimeZone.UTC)
    Database.init()
    system.actorOf(Props[Router], ActorNames.Router)
    system.actorOf(Props[SessionManager], ActorNames.SessionManager)

  }

  private def bind()(implicit system: ActorSystem): Unit = {
    val dispatcher = system.actorOf(Props[RequestDispatcher], "RequestDispatcher")
    IO(Http).tell(Http.Bind(
      dispatcher,
      config.getString("server.http.host"),
      config.getInt("server.http.port")
    ), dispatcher)
  }

  /**
   * Create all necessary directories for web gallery
   */
  private def mkdirs(): Unit = {
    def mkdir(path: String) = {
      new File(path).mkdirs()
    }

    mkdir(Configs.OriginalsDir)
    mkdir(Configs.AlternativesDir)
    mkdir(Configs.UnprocessedDir)
  }
}
