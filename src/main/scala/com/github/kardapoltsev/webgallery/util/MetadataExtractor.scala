package com.github.kardapoltsev.webgallery.util

import java.io.File
import com.github.kardapoltsev.webgallery.db.gen.Metadata
import com.drew.imaging.{ImageProcessingException, ImageMetadataReader}
import com.drew.metadata.exif.ExifIFD0Directory
import scala.util.control.NonFatal
import org.joda.time.{DateTimeZone, DateTime}
import com.github.kardapoltsev.webgallery.db.ExifMetadata



/**
 * Created by alexey on 5/27/14.
 */
object MetadataExtractor {
  def process(file: File): Option[ExifMetadata] = {
    try {
      val meta = ImageMetadataReader.readMetadata(file)
      val ifd0 = meta.getDirectory(classOf[ExifIFD0Directory])
      val cameraModel = ifd0.getString(ExifIFD0Directory.TAG_MODEL)
      val date = new DateTime(ifd0.getDate(ExifIFD0Directory.TAG_DATETIME), DateTimeZone.UTC)
      Some(ExifMetadata(Some(cameraModel), Some(date)))
    } catch {
      case NonFatal(e) => None
    }
  }
}
