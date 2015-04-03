package com.github.kardapoltsev.webgallery

import akka.util.Timeout
import com.typesafe.config.ConfigFactory

import scala.reflect.io.File

/**
 * Created by alexey on 5/26/14.
 */
object Configs {
  import concurrent.duration._

  private val defaultConfig = ConfigFactory.load()
  val config = ConfigFactory.parseFile(File("conf/application.conf").jfile).withFallback(defaultConfig)
  val OriginalsDir = config.getString("server.images.originals.dir")
  val AlternativesDir = config.getString("server.images.alternatives.dir")
  val UnprocessedDir = config.getString("server.images.unprocessed.dir")
  val MinPasswordLength = config.getInt("server.min-password-length")
  val MinLoginLength = config.getInt("server.min-login-length")
  val Mode = if (config.hasPath("server.application-mode")) {
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
    implicit val LongRunning = Timeout(20 seconds)
    implicit val Realtime = Timeout(1 second)
  }
  val RequestTimeout = config.getInt("server.http.request-timeout").seconds
}

object ApplicationMode extends Enumeration {
  val Dev = Value("dev")
  val Prod = Value("prod")
}