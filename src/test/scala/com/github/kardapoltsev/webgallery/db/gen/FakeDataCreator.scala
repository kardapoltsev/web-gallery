package com.github.kardapoltsev.webgallery.db.gen


import com.github.kardapoltsev.webgallery.db.UserId
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
  val userId2 = 12345
  val credentialsId = 123
  val sessionId = 123
  val aclId = 123
  val commentId = 123
  val likeId = 123
  val settingsId = 123


  def createImage2()(implicit s: DBSession) = createImage(imageId2)
  def createImage()(implicit s: DBSession): Boolean = createImage(imageId)

  def createLike()(implicit s: DBSession): Unit = {
    createImage()
    sql"insert into likes(id, image_id, owner_id) values ($commentId, $imageId, $userId)".execute().apply()
  }
  
  private def createImage(id: Int)(implicit s: DBSession): Boolean = {
    createUser()
    sql"insert into images(id, name, filename, owner_id) values ($id, 'name', 'filename', $userId)".execute().apply()
  }

  def createTag()(implicit s: DBSession) = {
    createUser()
    sql"insert into tags(id, owner_id, name, update_time) values ($tagId, $userId, $imageId, ${new Date()})".execute().apply()
  }

  def createComment()(implicit s: DBSession) = {
    createImage()
    sql"insert into comment(id, image_id, parent_comment_id, text, create_time, owner_id) values($commentId, $imageId, null, '', ${new Date()}, $userId)".execute().apply()
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

  protected def createSettings()(implicit s: DBSession) = {
    sql"insert into settings(id, version) values ($settingsId, 0)".execute().apply()
  }

  def createUser()(implicit s: DBSession): Unit = createUser(userId)
  def createUser2()(implicit s: DBSession): Unit = createUser(userId2)
  private def createUser(id: UserId)(implicit s: DBSession): Unit = {
    val u = User.find(id)
    if(u.isEmpty) sql"""insert into users (id, name, avatar_id, registration_time, search_info) values ($id, 'user', 0, ${new Date()}, to_tsvector('user'))""".execute().apply()
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
