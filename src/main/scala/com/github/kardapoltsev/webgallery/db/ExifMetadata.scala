package com.github.kardapoltsev.webgallery.db

import org.joda.time.DateTime

/**
 * Created by alexey on 6/8/14.
 */
case class ExifMetadata (
    cameraModel: Option[String] = None,
    creationTime: Option[DateTime] = None
    )

