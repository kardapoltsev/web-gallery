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
  import BaseSprayService._

  implicit def executionContext: ExecutionContext
  implicit def requestTimeout: Timeout

  protected def createTag(r: CreateTag): Result[CreateTagResponse] = processRequest(r)
  protected def getTags(r: GetTags.type): Result[GetTagsResponse] = processRequest(r)

  val tagsRoute: Route = respondWithMediaType(MediaTypes.`application/json`) {
    pathPrefix("api") {
      path("tags") {
        get {
          dynamic {
            handleWith {
              getTags
            }
          }
        } ~
        post {
          dynamic {
            handleWith {
              createTag
            }
          }
        }
      }
    }
  }
}
