package com.github.kardapoltsev.webgallery.http


import com.github.kardapoltsev.webgallery.AclManager.{GetGranteesResponse}
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
      val tag = getImage(imageId).tags.head
      getGrantees(tag.id).length should be(0)
    }
  }

  it should "grant access via PUT /api/acl/tag/{tagId}" in {
    authorized { implicit auth =>
      val imageId = createImage
      val tag = getImage(imageId).tags.head
      val userId = randomUserId
      addGrantees(tag.id, userId)
      getGrantees(tag.id).length should be(1)
    }
  }

  it should "not grant access twice" in {
    authorized { implicit auth =>
      val imageId = createImage
      val tag = getImage(imageId).tags.head
      val userId = randomUserId
      addGrantees(tag.id, userId)
      addGrantees(tag.id, userId)
      getGrantees(tag.id).length should be(1)
    }
  }

  it should "revoke access via DELETE /api/acl/tag/{tagId}" in {
    authorized { implicit auth =>
      val imageId = createImage
      val tag = getImage(imageId).tags.head
      val userId = randomUserId
      addGrantees(tag.id, userId)
      getGrantees(tag.id).length should be(1)
      deleteGrantees(tag.id, userId)
      getGrantees(tag.id).length should be(0)
    }
  }


  private def getGrantees(tagId: TagId)(implicit auth: AuthResponse): Seq[User] = {
    withCookie(Get(s"/api/acl/tag/$tagId")) ~> aclRoute ~> check {
      status should be(StatusCodes.OK)
      contentType should be(ContentTypes.`application/json`)
      responseAs[GetGranteesResponse].users
    }
  }


  private def addGrantees(tagId: TagId, userId: UserId)(implicit auth: AuthResponse): Unit = {
    val request =
      Put(s"/api/acl/tag/$tagId", HttpEntity(ContentTypes.`application/json`, JsArray(JsNumber(userId)).compactPrint))
    withCookie(request) ~> aclRoute ~> check {
      status should be(StatusCodes.OK)
    }
  }


  private def deleteGrantees(tagId: TagId, userId: UserId)(implicit auth: AuthResponse): Unit = {
    val request =
      Delete(s"/api/acl/tag/$tagId", HttpEntity(ContentTypes.`application/json`, JsArray(JsNumber(userId)).compactPrint))

    withCookie(request) ~> aclRoute ~> check {
      status should be(StatusCodes.OK)
    }
  }

}
