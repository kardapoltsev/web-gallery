package com.github.kardapoltsev.webgallery.util

import org.scalatest.{Matchers, FlatSpec}
import java.io.File
import com.github.kardapoltsev.webgallery.db.Metadata
import com.github.kardapoltsev.webgallery.TestFiles

/**
 * Created by alexey on 5/27/14.
 */
class MetadataExtractorTest extends FlatSpec with Matchers with TestFiles {
  "MetadataExtractor" should  "extract metadata" in {
    val meta = MetadataExtractor.process(dsc2845)
    //different time zone in travis?
    meta.get.cameraModel should be (dsc2845Metadata.cameraModel)
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
