package com.github.kardapoltsev.webgallery


import akka.util.Timeout
import com.typesafe.config.ConfigFactory

/**
 * Created by alexey on 5/26/14.
 */
object Configs {
  private val config = ConfigFactory.load()
  val OriginalsDir = config.getString("server.images.originals.dir")
  val AlternativesDir = config.getString("server.images.alternatives.dir")
  val UnprocessedDir = config.getString("server.images.unprocessed.dir")
  val Mode = if(config.hasPath("server.application-mode")) {
    try {
      ApplicationMode.withName(config.getString("server.application-mode"))
    } catch {
      case e: NoSuchElementException =>
      ApplicationMode.Dev
    }
  } else ApplicationMode.Dev

  object Timeouts {
    import concurrent.duration._
    implicit val Background = Timeout(60 seconds)
    implicit val LongRunning = Timeout(10 seconds)
    implicit val Realtime = Timeout(1 second)
  }
}


object ApplicationMode extends Enumeration {
  val Dev = Value("dev")
  val Prod = Value("prod")
}