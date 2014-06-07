package com.github.kardapoltsev.webgallery.util

import java.io.File
import com.github.kardapoltsev.webgallery.db.Metadata
import com.drew.imaging.{ImageProcessingException, ImageMetadataReader}
import com.drew.metadata.exif.ExifIFD0Directory
import scala.util.control.NonFatal

/**
 * Created by alexey on 5/27/14.
 */
object MetadataExtractor {
  def process(file: File): Option[Metadata] = {
    try {
      val meta = ImageMetadataReader.readMetadata(file)
      val ifd0 = meta.getDirectory(classOf[ExifIFD0Directory])
      val cameraModel = ifd0.getString(ExifIFD0Directory.TAG_MODEL)
      val date = ifd0.getDate(ExifIFD0Directory.TAG_DATETIME)
      Some(Metadata(cameraModel, date))
    } catch {
      case NonFatal(e) => None
    }
  }
}
