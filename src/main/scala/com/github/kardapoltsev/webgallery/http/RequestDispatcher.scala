package com.github.kardapoltsev.webgallery.http

import spray.routing.HttpService
import akka.actor._
import spray.can.Http
import akka.pattern.ask
import akka.util.Timeout
import com.github.kardapoltsev.webgallery.{WebGalleryActorSelection, Database}
import com.github.kardapoltsev.webgallery.Database._
import concurrent.{ExecutionContext, Future}
import com.github.kardapoltsev.webgallery.db.{Alternative, Tag, Image}
import com.github.kardapoltsev.webgallery.Database.GetTagsResponse
import com.github.kardapoltsev.webgallery.Database.GetImagesResponse
import com.github.kardapoltsev.webgallery.Database.CreateTagResponse
import com.github.kardapoltsev.webgallery.Database.GetImageResponse
import com.github.kardapoltsev.webgallery.ImageProcessor.{TransformImageRequest, TransformImageResponse}
import com.github.kardapoltsev.webgallery.dto.ImageInfo
import scala.util.control.NonFatal


/**
 * Created by alexey on 5/5/14.
 */
class RequestDispatcher extends Actor with HttpService with BaseSprayService with ActorLogging
  with ImagesSprayService with SearchSprayService with TagsSprayService
  with StaticSprayService with WebGalleryActorSelection {

  import BaseSprayService._

  def actorRefFactory: ActorContext = context

  //Note, that staticResourcesRoute should be last because it'll serve index.html on all unmatched requests
  def receive: Receive = serviceMessage orElse runRoute(imagesRoute ~ searchRoute ~ tagsRoute ~ staticResourcesRoute)


  import concurrent.duration._
  override implicit val executionContext: ExecutionContext = context.dispatcher
  override implicit val requestTimeout = Timeout(FiniteDuration(5, concurrent.duration.SECONDS))

  override def cwd = System.getProperty("user.dir")


  override protected def createTag(r: CreateTag): Result[CreateTagResponse] = askFrom(databaseSelection, r)
  override protected def getTags(r: GetTags.type): Result[GetTagsResponse] = askFrom(databaseSelection, r)


  //TODO: implement success response, ask and pipe it
  override protected def updateImage(request: UpdateImage): Future[InternalResponse] = {
    databaseSelection ? request map {
      case r: InternalResponse => r
    }
  }


  override protected def getImage(imageId: Int): Future[Option[ImageInfo]] = {
    databaseSelection ? Database.GetImage(imageId) map {
      case GetImageResponse(image) => image
    }
  }


  override protected def transformImage(request: TransformImageRequest): Future[Alternative] = {
    implicit val requestTimeout = Timeout(FiniteDuration(10, concurrent.duration.SECONDS))
    imageProcessorSelection ? request map {
      case response: TransformImageResponse => response.alternative
    }
  }


  override protected def getByTag(tag: String): Future[Seq[ImageInfo]] = {
    databaseSelection ? Database.GetByTag(tag) map {
      case GetImagesResponse(images) => images
    }
  }


  override protected def searchTags(query: String): Result[GetTagsResponse] =
    askFrom(databaseSelection, SearchTags(query))


  def serviceMessage: Receive = {
    case Http.Bound(address) =>
      log.info(s"RequestDispatcher successfully bound to $address")
  }
}
