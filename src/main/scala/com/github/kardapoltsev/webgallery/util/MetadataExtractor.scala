package com.github.kardapoltsev.webgallery.util

import java.io.File
import com.github.kardapoltsev.webgallery.db.Metadata
import com.drew.imaging.{ImageProcessingException, ImageMetadataReader}
import com.drew.metadata.exif.{ExifIFD0Directory, ExifSubIFDDirectory}
import java.text.SimpleDateFormat
import com.drew.imaging.jpeg.JpegProcessingException

/**
 * Created by alexey on 5/27/14.
 */
object MetadataExtractor {
  def process(file: File): Option[Metadata] = {
    try {
      val meta = ImageMetadataReader.readMetadata(file)
      val subIFD = meta.getDirectory(classOf[ExifSubIFDDirectory])
      val date = subIFD.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL)
      val ifd0 = meta.getDirectory(classOf[ExifIFD0Directory])
      val cameraModel = ifd0.getString(ExifIFD0Directory.TAG_MODEL)

      val album = new SimpleDateFormat("yyyy-MM-dd").format(date)
      Some(Metadata(cameraModel, Seq(album)))
    } catch {
      case e: JpegProcessingException => None
      case e: ImageProcessingException => None
    }
  }
}
