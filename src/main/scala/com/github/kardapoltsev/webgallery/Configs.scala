package com.github.kardapoltsev.webgallery

import com.typesafe.config.ConfigFactory

/**
 * Created by alexey on 5/26/14.
 */
object Configs {
  private val config = ConfigFactory.load()
  val OriginalsDir = config.getString("server.images.originals.dir")
  val ThumbnailsDir = config.getString("server.images.thumbnails.dir")
}
