package com.github.kardapoltsev.webgallery.db

import org.scalatest.{Matchers, FlatSpec}
import java.util.UUID
import com.github.kardapoltsev.webgallery.{Database, TestFiles}

/**
 * Created by alexey on 6/6/14.
 */
class ImageAlternativeCRUDTest extends FlatSpec with Matchers with TestFiles {
  "Database" should "create image alternative" in {

    val image = dsc2845Image

    Database.db.transaction { implicit s =>
      Metadata.insert(dsc2845Metadata)
      Image.insert(image)

      val alt = ImageAlternative(image.id, UUID.randomUUID().toString, TransformImageParams(100, 100, false))
      ImageAlternative.create(alt)
      val alt2 = ImageAlternative.getById(alt.id)

      alt2 should be('defined)

      alt2.get should be(alt)

      Image.deleteById(image.id)
    }
  }
}
