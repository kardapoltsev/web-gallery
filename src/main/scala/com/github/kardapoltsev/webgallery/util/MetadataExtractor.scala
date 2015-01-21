package com.github.kardapoltsev.webgallery.util

import java.io.File
import java.util.TimeZone
import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.exif.{ ExifSubIFDDirectory, ExifIFD0Directory }
import org.joda.time.format.DateTimeFormat
import org.slf4j.LoggerFactory
import scala.util.control.NonFatal
import org.joda.time.{ DateTimeZone, DateTime }
import com.github.kardapoltsev.webgallery.db.ImageMetadata
import com.drew.metadata.iptc.IptcDirectory
import com.drew.metadata.Metadata

/**
 * Created by alexey on 5/27/14.
 */
object MetadataExtractor {
  private val log = LoggerFactory.getLogger(this.getClass)
  def process(file: File): Option[ImageMetadata] = {
    try {
      val meta = ImageMetadataReader.readMetadata(file)
      val ifd0 = meta.getDirectory(classOf[ExifIFD0Directory])
      val exifSubFDD = meta.getDirectory(classOf[ExifSubIFDDirectory])
      val cameraModel = Option(ifd0.getString(ExifIFD0Directory.TAG_MODEL))
      val date = Option(ifd0.getDate(ExifIFD0Directory.TAG_DATETIME, TimeZone.getTimeZone("UTC"))).map(d =>
        new DateTime(d, DateTimeZone.UTC))
      val iso = Option(exifSubFDD.getInt(ExifSubIFDDirectory.TAG_ISO_EQUIVALENT))
      val lensModel = Option(exifSubFDD.getString(ExifSubIFDDirectory.TAG_LENS_MODEL))
      val keywords = extractKeywords(meta)
      log.debug(s"iso: $iso , lensModel: $lensModel")
      Some(ImageMetadata(cameraModel, date, iso, lensModel, keywords))
    } catch {
      case e: Exception =>
        log.error("couldn't extract metadata", e)
        None
    }
  }

  private def extractKeywords(meta: Metadata): Seq[String] = {
    import collection.JavaConversions._
    Option(meta.getDirectory(classOf[IptcDirectory])).flatMap(m => Option(m.getKeywords))
      .fold(Seq.empty[String])(_.toSeq)
  }

  def extractTags(m: ImageMetadata): Seq[String] = {
    m.keywords ++ Seq(
      m.cameraModel,
      m.creationTime.map(d => DateTimeFormat.forPattern("yyyy-MM-dd").print(d))
    ).flatten
  }.map(_.toLowerCase)

}
