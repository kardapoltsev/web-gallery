package com.github.kardapoltsev.webgallery.db

import spray.json.DefaultJsonProtocol

/**
 * Created by koko on 21/01/15.
 */
case class Stats(
  users: Int,
  images: Int,
  comments: Int,
  likes: Int)

object Stats extends DefaultJsonProtocol {
  implicit val _ = jsonFormat4(Stats.apply)
}