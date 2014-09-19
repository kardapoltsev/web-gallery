package com.github.kardapoltsev.webgallery.db

import com.github.kardapoltsev.webgallery.util.Hardcoded
import org.slf4j.LoggerFactory

import scala.io.Source



/**
 * Created by alexey on 5/26/14.
 */
object Database {

  def cleanDatabase(): Unit = {
    import scalikejdbc._
    DB autoCommit { implicit s =>
      sql"delete from settings; delete from tags; delete from images; delete from users; delete from credentials; delete from sessions;".execute().apply()
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
  import scalikejdbc._
  private val log = LoggerFactory.getLogger(this.getClass)
  type Update = (DBSession) => Unit
  private val updates = collection.mutable.Map[Int, Update]()

  //make this def for test purpose only, since we drop and init db many time during test
  private def currentVersion = try {
    Settings.findAll() match {
      case Nil => 0
      case List(settings) => settings.version
    }
  } catch {
    case e: Exception =>
      createScheme()
      0
  }

  private def createScheme(): Unit = {
    val initFile = getClass.getResourceAsStream("/initdb.sql")
    DB localTx { implicit s =>
      Source.fromInputStream(initFile).getLines().mkString(" ").split(";").foreach { query =>
        log.debug(query)
        SQL(query).execute().apply()
      }
    }
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
    sql"""select nextval('images_id_seq');""".execute().apply()
    sql"insert into images (id, name, filename, owner_id) values (${Hardcoded.DefaultAvatarId}, 'default_avatar.jpg', 'default_avatar.jpg', ${Hardcoded.RootUserId});".execute().apply()
    //default cover
    sql"""select nextval('images_id_seq');""".execute().apply()
    sql"insert into images (id, name, filename, owner_id) values (${Hardcoded.DefaultCoverId}, 'default_cover.jpg', 'default_cover.jpg', ${Hardcoded.RootUserId});".execute().apply()
    //database version
    sql"""select nextval('settings_id_seq');""".execute().apply()
    sql"insert into settings (version) values (0);".execute().apply()
  }


  def runUpdate(): Unit = {
    val cv = currentVersion
    val targetVersion = updates.keySet.max
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


  private def registerUpdate(version: Int)(u: Update): Unit =
    updates += version -> u
}
