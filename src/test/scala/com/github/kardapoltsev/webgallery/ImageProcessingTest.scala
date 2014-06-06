package com.github.kardapoltsev.webgallery

import org.scalatest.{WordSpec, Matchers}
import com.github.kardapoltsev.webgallery.processing.Java2DImageImplicits


/**
 * Created by alexey on 6/6/14.
 */
class ImageProcessingTest extends WordSpec with Matchers with TestFiles {

  import Java2DImageImplicits._


  "Java2DImage" should {
    "crop to size" in {
      val crop = imageFrom(dsc2845) croppedToSize (100 by 200)

      crop.dimensions.width should be(100)
    }
  }
}
