package com.github.kardapoltsev.webgallery


import org.scalatest.{Matchers, FlatSpec}
import com.github.kardapoltsev.webgallery.db.{ImagesTags, Metadata, Tag, Image}
import java.util.Date



/**
 * Created by alexey on 5/29/14.
 */
class ImageCRUDTest extends FlatSpec with Matchers {

  "Database" should "create and delete image" in {
    Database.db.transaction { implicit session =>
      val image = Image("test image", Seq.empty, null, "fname")

      Image.insert(image)
      val image2 = Image.getById(image.id)
      image2 should be('defined)
      image2.get should be(image)
      Image.deleteById(image.id)
      val image3 = Image.getById(image.id)
      image3 should be('empty)
    }
  }


  "Database" should "add tags to image" in {
    Database.db.transaction { implicit s =>
      val tag = Tag("friend")
      Tag.insert(tag)
      val image = Image("test image", Seq(tag), null, "fname")
      Image.insert(image)

      Image.addTag(ImagesTags(image.id, tag.id))
      val image2 = Image.getByTag(tag.name)
      image2 should have size 1
      image2.head should be(image)

      Image.deleteById(image.id)
      Tag.deleteById(tag.id)
    }
  }
}
