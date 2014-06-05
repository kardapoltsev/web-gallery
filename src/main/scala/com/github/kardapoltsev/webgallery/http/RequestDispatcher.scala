package com.github.kardapoltsev.webgallery.http

import spray.routing.HttpService
import akka.actor._
import spray.can.Http
import akka.pattern.ask
import akka.util.Timeout
import com.github.kardapoltsev.webgallery.{WebGalleryActorSelection, Database}
import com.github.kardapoltsev.webgallery.Database.{CreateTagResponse, GetImageResponse, GetTagsResponse, GetFilesResponse}
import concurrent.{ExecutionContext, Future}
import com.github.kardapoltsev.webgallery.db.{Tag, Image}



/**
 * Created by alexey on 5/5/14.
 */
class RequestDispatcher extends Actor with HttpService with ActorLogging
  with ImagesSprayService with SearchSprayService with TagsSprayService
  with StaticSprayService with WebGalleryActorSelection {

  def actorRefFactory: ActorContext = context

  //Note, that staticResourcesRoute should be last because it'll serve index.html on all unmatched requests
  def receive: Receive = serviceMessage orElse runRoute(imagesRoute ~ searchRoute ~ tagsRoute ~ staticResourcesRoute)


  import concurrent.duration._
  override implicit val executionContext: ExecutionContext = context.dispatcher
  override implicit val requestTimeout = Timeout(FiniteDuration(3, concurrent.duration.SECONDS))

  override def cwd = System.getProperty("user.dir")


  override protected def createTag(request: Database.CreateTag): Future[Tag] = {
    databaseSelection ? request map {
      case CreateTagResponse(t) => t
    }
  }


  override protected def getTags: Future[Seq[Tag]] = {
    databaseSelection ? Database.GetTags map {
      case GetTagsResponse(tags) => tags
    }
  }


  override protected def addTags(imageId: Int, tags: Seq[String]): Unit = {
    databaseSelection ! Database.AddTags(imageId, tags)
  }


  override protected def getImage(imageId: Int): Future[Option[Image]] = {
    databaseSelection ? Database.GetImage(imageId) map {
      case GetImageResponse(image) => image
    }
  }


  override protected def getByTag(tag: String): Future[Seq[Image]] = {
    databaseSelection ? Database.GetByTag(tag) map {
      case GetFilesResponse(images) => images
    }
  }


  override protected def searchTags(query: String): Future[Seq[String]] = {
    databaseSelection ? Database.SearchTags(query) map {
      case GetTagsResponse(tags) => tags.map(_.name)
    }
  }


  def serviceMessage: Receive = {
    case Http.Bound(address) =>
      log.info(s"RequestDispatcher successfully bound to $address")
  }
}
