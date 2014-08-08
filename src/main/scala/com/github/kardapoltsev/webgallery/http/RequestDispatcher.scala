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
import com.github.kardapoltsev.webgallery.UserManager._
import com.github.kardapoltsev.webgallery.Database.SearchTags
import com.github.kardapoltsev.webgallery.Database.CreateTag
import com.github.kardapoltsev.webgallery.Database.GetImagesResponse
import com.github.kardapoltsev.webgallery.Database.CreateTagResponse
import com.github.kardapoltsev.webgallery.Database.UpdateImage
import com.github.kardapoltsev.webgallery.ImageProcessor.TransformImageResponse
import com.github.kardapoltsev.webgallery.ImageProcessor.TransformImageRequest
import com.github.kardapoltsev.webgallery.Database.GetImage
import com.github.kardapoltsev.webgallery.util.Hardcoded


/**
 * Actor that will bind to http port and process http requests
 */
class RequestDispatcher extends Actor with HttpService with BaseSprayService with ActorLogging
  with ImagesSprayService with SearchSprayService with TagsSprayService
  with StaticSprayService with UserSprayService with AuthSprayService {

  import BaseSprayService._
  import WebGalleryActorSelection.routerSelection

  def actorRefFactory: ActorContext = context

  //Note, that staticResourcesRoute should be last because it'll serve index.html on all unmatched requests
  def receive: Receive = serviceMessage orElse runRoute(
    imagesRoute ~ searchRoute ~ tagsRoute ~ usersRoute ~ authRoute ~ staticResourcesRoute
  )


  import concurrent.duration._
  override implicit val executionContext: ExecutionContext = context.dispatcher
  override implicit val requestTimeout = Timeout(FiniteDuration(5, concurrent.duration.SECONDS))

  override def cwd = System.getProperty("user.dir")


  def serviceMessage: Receive = {
    case Http.Bound(address) =>
      log.info(s"RequestDispatcher successfully bound to $address")
  }

}
