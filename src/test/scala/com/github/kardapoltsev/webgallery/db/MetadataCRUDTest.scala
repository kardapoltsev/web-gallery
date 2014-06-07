package com.github.kardapoltsev.webgallery.db

import org.scalatest.{BeforeAndAfterEach, Matchers, FlatSpec}
import java.util.Date
import com.github.kardapoltsev.webgallery.Database


/**
 * Created by alexey on 5/29/14.
 */
class MetadataCRUDTest extends FlatSpec with Matchers with BeforeAndAfterEach {

  override def afterEach(): Unit = {
    Database.db.transaction { implicit s =>
      Database.cleanDatabase()
    }
  }


  "Database" should "create and delete metadata" in {
    Database.db.transaction{ implicit session =>
      val meta = Metadata("NIKON D7000", new Date())
      Metadata.insert(meta)
      val meta2 = Metadata.selectById(meta.id)
      meta2 should be('defined)
      meta2.get should be(meta)
      Metadata.deleteById(meta.id)
      val meta3 = Metadata.selectById(meta.id)
      meta3 should be('empty)
    }
  }
}
