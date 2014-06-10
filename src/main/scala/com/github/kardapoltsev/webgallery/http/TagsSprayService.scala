package com.github.kardapoltsev.webgallery.http

import spray.routing.{Route, HttpService}
import scala.concurrent.{Future, ExecutionContext}
import akka.util.Timeout
import spray.http._
import spray.json._
import com.github.kardapoltsev.webgallery.db.Tag
import com.github.kardapoltsev.webgallery.Database._
import com.github.kardapoltsev.webgallery.Database.CreateTag
import com.github.kardapoltsev.webgallery.Database.GetTagsResponse
import com.github.kardapoltsev.webgallery.Database.CreateTagResponse


/**
 * Created by alexey on 6/4/14.
 */
trait TagsSprayService extends BaseSprayService { this: HttpService =>
  import marshalling._
  implicit def executionContext: ExecutionContext
  implicit def requestTimeout: Timeout

  protected def getTags: GetTags.type => Result[GetTagsResponse]
  protected def createTag: CreateTag => Result[CreateTagResponse]

  val tagsRoute: Route = respondWithMediaType(MediaTypes.`application/json`) {
    pathPrefix("api") {
      path("tags") {
        get {
          respond(getTags)
        } ~
        post {
          respond(createTag)
        }
      }
    }
  }
}
