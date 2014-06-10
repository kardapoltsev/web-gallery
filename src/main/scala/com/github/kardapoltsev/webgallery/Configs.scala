package com.github.kardapoltsev.webgallery

import com.typesafe.config.ConfigFactory

/**
 * Created by alexey on 5/26/14.
 */
object Configs {
  private val config = ConfigFactory.load()
  val OriginalsDir = config.getString("server.images.originals.dir")
  val AlternativesDir = config.getString("server.images.alternatives.dir")
  val UnprocessedDir = config.getString("server.images.unprocessed.dir")

  val CheckUnprocessedInterval = config.getInt("server.images.intervals.check-unprocessed")
}
