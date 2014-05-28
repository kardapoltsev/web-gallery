package com.github.kardapoltsev.webgallery

import akka.actor.{Props, ActorSystem}
import com.github.kardapoltsev.webgallery.http.RequestDispatcher
import akka.io.IO
import spray.can.Http
import com.typesafe.config.ConfigFactory



/**
 * Created by alexey on 5/5/14.
 */
object Server {
  object Names {
    val Database = "Database"
    val ImageProcessor = "ImageProcessor"
  }
  val config = ConfigFactory.load()

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("server")

    system.actorOf(Props[Database], Names.Database)
    system.actorOf(Props[ImageProcessor], Names.ImageProcessor)

    val dispatcher = system.actorOf(Props[RequestDispatcher], "RequestDispatcher")
    IO(Http).tell(Http.Bind(
      dispatcher,
      config.getString("server.http.host"),
      config.getInt("server.http.port")
    ), dispatcher)
  }
}
