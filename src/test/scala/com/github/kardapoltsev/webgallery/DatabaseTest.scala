package com.github.kardapoltsev.webgallery


import org.scalatest.{Matchers, FlatSpec}
import com.github.kardapoltsev.webgallery.db.{Metadata, Tag, Image}
import java.util.Date



/**
 * Created by alexey on 5/29/14.
 */
class DatabaseTest extends FlatSpec with Matchers {

  "Database" should "create and delete image" in {
    Database.context.transaction { implicit session =>
      val image = Image("test image", Seq.empty, null)

      Image.insertImage(image)
      val image2 = Image.selectById(image.id)
      image2 should be('defined)
      image2.get should be(image)
      Image.deleteById(image.id)
      val image3 = Image.selectById(image.id)
      image3 should be('empty)
    }
  }
}
