package com.github.kardapoltsev.webgallery.util

import org.scalatest.{Matchers, FlatSpec}
import java.io.File
import com.github.kardapoltsev.webgallery.db.Metadata

/**
 * Created by alexey on 5/27/14.
 */
class MetadataExtractorTest extends FlatSpec with Matchers {
  "MetadataExtractor" should  "extract metadata" in {
      val image = new File(getClass.getResource("/DSC_2845.jpg").toURI)
      val meta = MetadataExtractor.process(image)

      meta should be (Metadata("NIKON D7000", Seq("2014-05-10")))
    }
}
