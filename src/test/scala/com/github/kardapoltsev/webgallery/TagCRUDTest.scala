package com.github.kardapoltsev.webgallery


import org.scalatest.{Matchers, FlatSpec}
import com.github.kardapoltsev.webgallery.db.Tag



/**
 * Created by alexey on 5/29/14.
 */
class TagCRUDTest extends FlatSpec with Matchers {
  "Database" should "create and delete tags" in {
    Database.context.transaction{ implicit session =>
      val tag = Tag("friend")
      Tag.insert(tag)
      val tag2 = Tag.selectById(tag.id)
      tag2 should be('defined)
      tag2.get should be(tag)
      Tag.deleteById(tag.id)
      val tag3 = Tag.selectById(tag.id)
      tag3 should be('empty)
    }
  }

  it should "search tags by name" in {
    Database.context.transaction { implicit session =>
      val tag = Tag("friend")
      Tag.insert(tag)
      val tag2 = Tag.selectByName(tag.name)
      tag2 should be('defined)
      tag2.get should be(tag)
      Tag.deleteById(tag.id)
    }
  }
}
