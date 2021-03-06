package com.github.kardapoltsev.webgallery

import java.io.File
import com.github.kardapoltsev.webgallery.db._
import java.util.UUID
import org.joda.time.{ DateTimeZone, DateTime }

/**
 * Created by alexey on 5/27/14.
 */
trait TestFiles {
  protected val dsc2845 = new File(getClass.getResource("/DSC_2845.jpg").toURI)
  protected val dsc2845Metadata = ImageMetadata(Some("NIKON D7000"), Some(DateTime.parse("2014-05-10T22:44:41.000Z")),
    Some(400), Some("18.0-55.0 mm f/3.5-5.6"), List())
  protected val dsc2845Image = Image(
    0, "DSC_2845.jpg", UUID.randomUUID().toString, 123)

  protected val scrotPng = new File(getClass.getResource("/scrot.png").toURI)
  protected val scrotJpg = new File(getClass.getResource("/scrot.jpg").toURI)

  protected val dummyTextFile = new File(getClass.getResource("/dummy_text.jpg").toURI)
}
