package com.github.kardapoltsev.webgallery.db

import org.scalatest.{BeforeAndAfterEach, Matchers, FlatSpec}
import java.util.UUID
import com.github.kardapoltsev.webgallery.{Database, TestFiles}

/**
 * Created by alexey on 6/6/14.
 */
class ImageAlternativeCRUDTest extends FlatSpec with Matchers with TestFiles with BeforeAndAfterEach {

  override def afterEach(): Unit = {
    Database.db.transaction { implicit s =>
      Database.cleanDatabase()
    }
  }


  "Database" should "create image alternative" in {

    val image = dsc2845Image

    Database.db.transaction { implicit s =>
      Metadata.insert(dsc2845Metadata)
      Image.insert(image)

      val alt = Alternative(image.id, UUID.randomUUID().toString, TransformImageParams(100, 100, false))
      Alternative.create(alt)
      val alt2 = Alternative.getById(alt.id)

      alt2 should be('defined)

      alt2.get should be(alt)
    }
  }
}
