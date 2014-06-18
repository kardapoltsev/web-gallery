package com.github.kardapoltsev.webgallery

import akka.actor.{Props, ActorSystem}
import com.github.kardapoltsev.webgallery.http.RequestDispatcher
import akka.io.IO
import spray.can.Http
import com.typesafe.config.{Config, ConfigFactory}
import java.io.File
import com.github.kardapoltsev.webgallery.routing.Router


/**
 * Created by alexey on 5/5/14.
 */
object Server {
  import com.github.kardapoltsev.webgallery.util.Hardcoded.ActorNames

  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load()

    mkdirs()
    //init connection pool
    Database
    initActorSystem(config)
  }


  def initActorSystem(config: Config): ActorSystem = {
    implicit val system = ActorSystem("server")

    system.actorOf(Props[Router], ActorNames.Router)

    val dispatcher = system.actorOf(Props[RequestDispatcher], "RequestDispatcher")
    IO(Http).tell(Http.Bind(
      dispatcher,
      config.getString("server.http.host"),
      config.getInt("server.http.port")
    ), dispatcher)
    system
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
