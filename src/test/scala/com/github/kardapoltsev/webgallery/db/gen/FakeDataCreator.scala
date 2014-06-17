package com.github.kardapoltsev.webgallery.db.gen


import scalikejdbc._



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


  def createImage2()(implicit s: DBSession) = createImage(imageId2)
  def createImage()(implicit s: DBSession): Boolean = createImage(imageId)
  
  private def createImage(id: Int)(implicit s: DBSession): Boolean = {
    createUser()
    sql"insert into image(id, name, filename, owner_id) values ($id, 'name', 'filename', $userId)".execute().apply()
  }

  def createTag()(implicit s: DBSession) = {
    sql"insert into tag(id, name) values($tagId, $imageId)".execute().apply()
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
    import java.util.Date
    sql"""insert into users (id, name, registration_time) (select $userId, 'user', ${new Date()} from users where not exists(select 1 from users where id = $userId))""".execute().apply()
  }

  def createCredentials()(implicit s: DBSession) = {
    createUser()
    sql"""insert into credentials (id, auth_id, auth_type, user_id) values($credentialsId, 'test', 'Direct', $userId)""".execute().apply()
  }

}
