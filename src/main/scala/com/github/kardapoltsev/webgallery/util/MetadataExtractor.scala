package com.github.kardapoltsev.webgallery.util

import java.io.File
import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.exif.ExifIFD0Directory
import scala.util.control.NonFatal
import org.joda.time.{DateTimeZone, DateTime}
import com.github.kardapoltsev.webgallery.db.ImageMetadata
import com.drew.metadata.iptc.IptcDirectory
import com.drew.metadata.Metadata



/**
 * Created by alexey on 5/27/14.
 */
object MetadataExtractor {
  def process(file: File): Option[ImageMetadata] = {
    try {
      val meta = ImageMetadataReader.readMetadata(file)
      val ifd0 = meta.getDirectory(classOf[ExifIFD0Directory])
      val cameraModel = Option(ifd0.getString(ExifIFD0Directory.TAG_MODEL))
      val date = Option(ifd0.getDate(ExifIFD0Directory.TAG_DATETIME)).map(d =>
        new DateTime(d, DateTimeZone.UTC))
      val keywords = extractKeywords(meta)
      Some(ImageMetadata(cameraModel, date, keywords))
    } catch {
      case NonFatal(e) =>
        e.printStackTrace()
        None
    }
  }


  private def extractKeywords(meta: Metadata): Seq[String] = {
    import collection.JavaConversions._
    Option(meta.getDirectory(classOf[IptcDirectory])).flatMap(m => Option(m.getKeywords))
        .fold(Seq.empty[String])(_.toSeq)
  }
}
