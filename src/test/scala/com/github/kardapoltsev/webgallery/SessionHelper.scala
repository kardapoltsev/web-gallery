package com.github.kardapoltsev.webgallery

import com.github.kardapoltsev.webgallery.db._
import com.github.kardapoltsev.webgallery.db.gen.FakeDataCreator
import org.joda.time.DateTime

/**
 * Created by alexey on 6/24/14.
 */
trait SessionHelper extends FakeDataCreator {
  def withSession[A](f: Session => A): A = {
    val s = Session(0, userId, DateTime.now)
    f(s)
  }
}
