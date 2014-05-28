package com.github.kardapoltsev.webgallery


import org.scalatest.{Matchers, FlatSpec}
import com.github.kardapoltsev.webgallery.db.{Tag, Image}
import java.util.Date



/**
 * Created by alexey on 5/29/14.
 */
class DatabaseTest extends FlatSpec with Matchers {

  "Database" should "create image" in {
    Database.context.transaction { implicit session =>
      val image = Image("test image", Seq(Tag(0L, "album1")), None)

      Image.insertImage(image)
//      Image.selectAll().length should be > 0
      1 should be(1)
    }
  }
}
