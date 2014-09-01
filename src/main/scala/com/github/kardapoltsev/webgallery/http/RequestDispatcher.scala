package com.github.kardapoltsev.webgallery.http


import com.github.kardapoltsev.webgallery.Configs
import spray.routing.HttpService
import akka.actor._
import spray.can.Http
import scala.concurrent.ExecutionContext


/**
 * Actor that will bind to http port and process http requests
 */
class RequestDispatcher extends Actor with HttpService with BaseSprayService with ActorLogging
  with ImagesSprayService with SearchSprayService with TagsSprayService
  with StaticSprayService with UserSprayService with AuthSprayService with AclSprayService
  with LikeSprayService {


  def actorRefFactory: ActorContext = context

  //Note, that staticResourcesRoute should be last because it'll serve index.html on all unmatched requests
  def receive: Receive = serviceMessage orElse runRoute(
    imagesRoute ~ searchRoute ~ tagsRoute ~ usersRoute ~ authRoute ~ aclRoute ~
      likeRoute ~ staticResourcesRoute
  )


  override implicit val executionContext: ExecutionContext = context.dispatcher
  override implicit val requestTimeout = Configs.Timeouts.LongRunning

  override def cwd = System.getProperty("user.dir")


  def serviceMessage: Receive = {
    case Http.Bound(address) =>
      log.info(s"RequestDispatcher successfully bound to $address")
  }

}
