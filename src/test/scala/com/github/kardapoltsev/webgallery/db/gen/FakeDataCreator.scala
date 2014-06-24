package com.github.kardapoltsev.webgallery.db.gen


import scalikejdbc._
import org.postgresql.util.PSQLException
import java.util.Date



/**
 * Created by alexey on 6/8/14.
 */
trait FakeDataCreator {
  val imageId = 123
  val imageId2 = 12345
  val tagId = 123
  val alternativeId = 123
  val metadataId = 123
  val userId = 123
  val credentialsId = 123
  val sessionId = 123
  val aclId = 123


  def createImage2()(implicit s: DBSession) = createImage(imageId2)
  def createImage()(implicit s: DBSession): Boolean = createImage(imageId)
  
  private def createImage(id: Int)(implicit s: DBSession): Boolean = {
    createUser()
    sql"insert into image(id, name, filename, owner_id) values ($id, 'name', 'filename', $userId)".execute().apply()
  }

  def createTag()(implicit s: DBSession) = {
    sql"insert into tags(id, name) values($tagId, $imageId)".execute().apply()
  }
  
  def createImageTag()(implicit s: DBSession) = {
    createImage
    createTag
    sql"insert into image_tag(image_id, tag_id) values($imageId, $tagId)".execute().apply()
  }

  def createAlternative()(implicit s: DBSession) = {
    createImage
    sql"insert into alternative(id, image_id, filename, width, height, scale_type) values($alternativeId, $imageId, '', 100, 100, 'FillDest')".execute().apply()
  }

  def createMetadata()(implicit s: DBSession) = {
    createImage
    sql"insert into metadata(id, image_id, camera_model, creation_time) values($metadataId, $imageId, '', null)".execute().apply()
  }

  def createUser()(implicit s: DBSession) = {
    val u = User.find(userId)
    if(u.isEmpty) sql"""insert into users (id, name, registration_time) values ($userId, 'user', ${new Date()})""".execute().apply()
  }

  def createCredentials()(implicit s: DBSession) = {
    createUser()
    sql"""insert into credentials (id, auth_id, auth_type, password_hash, user_id) values($credentialsId, 'test', 'Direct', 'hash', $userId)""".execute().apply()
  }

  def createSession()(implicit s: DBSession) = {
    createUser()
    sql"""insert into sessions (id, user_id, update_time) values($sessionId, $userId, ${new Date()})""".execute().apply()
  }

  def createAcl()(implicit s: DBSession) = {
    createUser()
    createTag()
    sql"""insert into acl (id, user_id, tag_id) values($aclId, $userId, $tagId)""".execute().apply()
  }

}
