package com.github.kardapoltsev.webgallery.util

import org.joda.time.{ DateTimeZone }
import org.scalatest.{ Matchers, FlatSpec }
import com.github.kardapoltsev.webgallery.TestFiles

/**
 * Created by alexey on 5/27/14.
 */
class MetadataExtractorTest extends FlatSpec with Matchers with TestFiles {
  DateTimeZone.setDefault(DateTimeZone.UTC)

  "MetadataExtractor" should "extract metadata" in {
    val meta = MetadataExtractor.process(dsc2845)
    //different time zone in travis?
    meta should be(Some(dsc2845Metadata))
  }

  it should "not throw exception on wrong images" in {
    val meta = MetadataExtractor.process(dummyTextFile)
    meta should be(None)

    val pngMeta = MetadataExtractor.process(scrotPng)
    pngMeta should be(None)

    val jpgMeta = MetadataExtractor.process(scrotJpg)
    jpgMeta should be(None)
  }
}
