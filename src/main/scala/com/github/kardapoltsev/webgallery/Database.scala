package com.github.kardapoltsev.webgallery

import akka.actor.{Actor, ActorLogging}
import com.github.kardapoltsev.webgallery.db._
import com.github.kardapoltsev.webgallery.util.Hardcoded
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import scalikejdbc.{DBSession, DB}
import spray.json.DefaultJsonProtocol
import scala.io.Source
import scala.util.control.NonFatal
import com.github.kardapoltsev.webgallery.processing.{OptionalSize, SpecificSize}
import com.github.kardapoltsev.webgallery.db.gen
import com.github.kardapoltsev.webgallery.dto.ImageInfo
import com.github.kardapoltsev.webgallery.http._
import akka.event.LoggingReceive
import com.github.kardapoltsev.webgallery.routing.DatabaseRequest



/**
 * Created by alexey on 5/26/14.
 */
class Database extends Actor with ActorLogging with ImageHelper {
  import Database._
  import scalikejdbc._


  def receive: Receive = LoggingReceive {
    case GetImage(imageId) =>
      getImage(imageId) match {
        case Some(image) => sender() ! GetImageResponse(image)
        case None => sender() ! ErrorResponse.NotFound
      }

    case r: UpdateImage =>
      updateImage(r)
      sender() ! SuccessResponse

    case r: GetByTag =>
      sender() ! GetImagesResponse(getImagesByTag(r.tagId, r.session.get.userId))

  }


  private def updateImage(r: UpdateImage) = {
    //TODO: insert only new tags and delete other tags
    r.params.tags.foreach { tags =>
      tags foreach { tag =>
        try {
          ImageTag.create(r.imageId, tag.id)
        } catch {
          case NonFatal(e) =>
        }
      }
    }
  }


  private def getImage(imageId: Int): Option[ImageInfo] = {
    Image.find(imageId) map { image =>
      val tags = Tag.findByImageId(image.id)
      ImageInfo(image, tags)
    }
  }


  private def getImagesByTag(tagId: TagId, userId: UserId): Seq[ImageInfo] = {
    log.debug(s"searching by tagId $tagId for userId $userId")
    Image.findByTag(tagId, userId) map { image =>
      val tags = Tag.findByImageId(image.id)
      ImageInfo(image, tags)
    }
  }

}


trait PrivilegedImageRequest extends PrivilegedRequest {
  def imageId: ImageId
  def subjectType = EntityType.Image
  def subjectId = imageId
}


object Database extends DefaultJsonProtocol {
  //Images
  case class GetImage(imageId: Int) extends AuthorizedRequest with DatabaseRequest
  case class GetByTag(tagId: TagId) extends AuthorizedRequest with DatabaseRequest
  case class GetImageResponse(image: ImageInfo)
  object GetImageResponse {
    implicit val _ = jsonFormat1(GetImageResponse.apply)
  }
  
  case class UpdateImageParams(tags: Option[Seq[Tag]])
  object UpdateImageParams {
    implicit val _ = jsonFormat1(UpdateImageParams.apply)
  }
  case class UpdateImage(imageId: Int, params: UpdateImageParams) extends PrivilegedImageRequest with DatabaseRequest
  object UpdateImage {
    implicit val _ = jsonFormat2(UpdateImage.apply)
  }
  case class GetImagesResponse(images: Seq[ImageInfo])
  object GetImagesResponse {
    implicit val _ = jsonFormat1(GetImagesResponse.apply)
  }


  def cleanDatabase(): Unit = {
    import scalikejdbc._
    DB autoCommit { implicit s =>
      sql"delete from settings; delete from image; delete from tags; delete from users; delete from credentials; delete from sessions;".execute().apply()
    }
    DatabaseUpdater.runUpdate()
  }


  def init(): Unit = {
    import scalikejdbc.config._
    DBs.setupAll()
    DatabaseUpdater.runUpdate()
  }
}


object DatabaseUpdater {
  import db._
  import scalikejdbc._
  private val log = LoggerFactory.getLogger(this.getClass)
  val targetVersion = 1
  type Update = (DBSession) => Unit
  private val updates = collection.mutable.Map[Int, Update]()

  //make this def for test purpose only, since we drop and init db many time during test
  private def currentVersion = try {
    Settings.findAll() match {
      case Nil => 0
      case List(settings) => settings.version
      case _ =>
        throw new Exception("Multiple records found in settings table")
    }
  } catch {
    case e: Exception =>
      throw new RuntimeException("Could not init database", e)
  }


  //populate database with initial data
  registerUpdate(1) { implicit session: DBSession =>
    //root user
    sql"""select nextval('users_id_seq');""".execute().apply()
    sql"insert into users (id, name, avatar_id, search_info) values (${Hardcoded.RootUserId}, 'root', ${Hardcoded.DefaultAvatarId}, to_tsvector('root'));".execute().apply()
    //anonymous
    sql"""select nextval('users_id_seq');""".execute().apply()
    sql"insert into users (id, name, avatar_id, search_info) values (${Hardcoded.AnonymousUserId}, 'anonymous', ${Hardcoded.DefaultAvatarId}, to_tsvector('anonymous'));".execute().apply()
    //default avatar
    sql"""select nextval('image_id_seq');""".execute().apply()
    sql"insert into image (id, name, filename, owner_id) values (${Hardcoded.DefaultAvatarId}, 'default_avatar.jpg', 'default_avatar.jpg', ${Hardcoded.RootUserId});".execute().apply()
    //database version
    sql"""select nextval('settings_id_seq');""".execute().apply()
    sql"insert into settings (version) values (0);".execute().apply()
  }


  def runUpdate(): Unit = {
    val cv = currentVersion
    log.debug(s"running database updates, current version is $cv")
    for (v <- cv + 1 to targetVersion) {
      log.info(s"updating db to version $v")
      DB localTx { implicit s =>
        updates(v)(s)
        Settings.setVersion(v)
      }
      log.info(s"db updated to version $v")
    }
  }


//  private def createScheme()(implicit s: DBSession): Unit ={
//    Source.fromFile("initdb.sql").getLines().foreach{ l =>
//      log.debug(l)
//      SQL(l).execute().apply()
//    }
//  }


  private def registerUpdate(version: Int)(u: Update): Unit =
    updates += version -> u
}
