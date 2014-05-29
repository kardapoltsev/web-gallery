package com.github.kardapoltsev.webgallery


import org.scalatest.{Matchers, FlatSpec}
import com.github.kardapoltsev.webgallery.db.Metadata
import java.util.Date



/**
 * Created by alexey on 5/29/14.
 */
class MetadataCRUDTest extends FlatSpec with Matchers {
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
