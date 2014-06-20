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
  with StaticSprayService with UserSprayService {

  import BaseSprayService._
  import WebGalleryActorSelection.routerSelection

  def actorRefFactory: ActorContext = context

  //Note, that staticResourcesRoute should be last because it'll serve index.html on all unmatched requests
  def receive: Receive = serviceMessage orElse runRoute(
    imagesRoute ~ searchRoute ~ tagsRoute ~ usersRoute ~ staticResourcesRoute
  )


  import concurrent.duration._
  override implicit val executionContext: ExecutionContext = context.dispatcher
  override implicit val requestTimeout = Timeout(FiniteDuration(5, concurrent.duration.SECONDS))

  override def cwd = System.getProperty("user.dir")


  override protected def createTag(r: CreateTag): Result[CreateTagResponse] = processRequest(r)
  override protected def getTags(r: GetTags.type): Result[GetTagsResponse] = processRequest(r)
  override protected def updateImage(r: UpdateImage): Result[SuccessResponse] = processRequest(r)
  override protected def getImage(r: GetImage): Result[GetImageResponse] = processRequest(r)
  override protected def searchTags(r: SearchTags): Result[GetTagsResponse] = processRequest(r)
  override protected def registerUser(r: RegisterUser): Result[RegisterUserResponse] = processRequest(r)
  override protected def auth(r: Auth): Result[AuthResponse] = processRequest(r)
  override protected def getUser(r: GetUser): Result[GetUserResponse] = processRequest(r)


  //TODO: refactor with askRouter
  override protected def transformImage(request: TransformImageRequest): Future[Alternative] = {
    implicit val requestTimeout = Timeout(FiniteDuration(10, concurrent.duration.SECONDS))
    routerSelection ? request map {
      case response: TransformImageResponse => response.alternative
    }
  }


  //TODO: refactor with askRouter
  override protected def getByTag(tag: String): Future[Seq[ImageInfo]] = {
    routerSelection ? Database.GetByTag(tag) map {
      case GetImagesResponse(images) => images
    }
  }




  def serviceMessage: Receive = {
    case Http.Bound(address) =>
      log.info(s"RequestDispatcher successfully bound to $address")
  }


}
