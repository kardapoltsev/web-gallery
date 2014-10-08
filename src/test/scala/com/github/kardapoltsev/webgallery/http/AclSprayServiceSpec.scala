package com.github.kardapoltsev.webgallery.http


import com.github.kardapoltsev.webgallery.acl.AclManager
import AclManager.{GetGranteesResponse}
import com.github.kardapoltsev.webgallery.UserManager.AuthResponse
import com.github.kardapoltsev.webgallery.db.{UserId, TagId, User}
import com.github.kardapoltsev.webgallery.{TestBase}
import spray.http.{HttpEntity, ContentTypes, StatusCodes}
import spray.json.{JsNumber, JsArray}




/**
 * Created by alexey on 5/30/14.
 */
class AclSprayServiceSpec extends TestBase with AclSprayService {


  import marshalling._


  behavior of "AclSprayService"

  it should "return grantees via GET /api/acl/tag/{tagId}" in {
    authorized { implicit auth =>
      val imageId = createImage
      waitForUpdates()
      val tag = getImage(imageId).tags.head
      getGrantees(tag.id).length should be(1)
    }
  }

  it should "grant access via PUT /api/acl/tag/{tagId}" in {
    authorized { implicit auth =>
      val imageId = createImage
      waitForUpdates()
      val tag = getImage(imageId).tags.head
      val userId = randomUserId
      addGrantees(tag.id, userId)
      getGrantees(tag.id).length should be(2)
    }
  }

  it should "not grant access twice" in {
    authorized { implicit auth =>
      val imageId = createImage
      waitForUpdates()
      val tag = getImage(imageId).tags.head
      val userId = randomUserId
      addGrantees(tag.id, userId)
      addGrantees(tag.id, userId)
      getGrantees(tag.id).length should be(2)
    }
  }

  it should "revoke access via DELETE /api/acl/tag/{tagId}" in {
    authorized { implicit auth =>
      val imageId = createImage
      waitForUpdates()
      val tag = getImage(imageId).tags.head
      val userId = randomUserId
      addGrantees(tag.id, userId)
      getGrantees(tag.id).length should be(2)
      deleteGrantees(tag.id, userId)
      getGrantees(tag.id).length should be(1)
    }
  }

}
