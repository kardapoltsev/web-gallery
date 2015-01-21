package com.github.kardapoltsev.webgallery.db

import org.joda.time.DateTime

/**
 * Created by alexey on 6/8/14.
 */
case class ImageMetadata(
  cameraModel: Option[String] = None,
  creationTime: Option[DateTime] = None,
  iso: Option[Int] = None,
  lensModel: Option[String] = None,
  /**
   * IPTC keywords
   */
  keywords: Seq[String] = Seq.empty)

