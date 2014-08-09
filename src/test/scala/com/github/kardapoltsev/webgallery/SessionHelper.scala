package com.github.kardapoltsev.webgallery

import com.github.kardapoltsev.webgallery.db._
import com.github.kardapoltsev.webgallery.db.gen.FakeDataCreator
import org.joda.time.DateTime

/**
 * Created by alexey on 6/24/14.
 */
trait SessionHelper extends FakeDataCreator {
  protected def withSession[A](f: Session => A): A = {
    val s = Session(0, userId, DateTime.now)
    f(s)
  }

  protected def withSession[A](userId: UserId)(f: Session => A): A = {
    val s = Session(0, userId, DateTime.now)
    f(s)
  }
}
