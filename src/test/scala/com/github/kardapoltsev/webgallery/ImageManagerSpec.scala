package com.github.kardapoltsev.webgallery


import org.scalatest.{Matchers, FlatSpec}



/**
 * Created by alexey on 6/12/14.
 */
class ImageManagerSpec extends FlatSpec with Matchers with TestFiles {

  behavior of "ImageProcessor"

  it should "extract tags from metadata" in {
    val tags = ImageManager.extractTags(dsc2845Metadata)
    tags should be(Seq("nikon d7000", "2014-05-10"))
  }

}
