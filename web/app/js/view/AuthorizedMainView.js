/**
 * Created by alexey on 6/4/14.
 */
define(function(require){

  var $ = require("jquery"),
      ImagePreviewList = require("collection/ImagePreviewList"),
      UserTagsList = require("collection/UserTagsList"),
      Backbone = require("backbone"),
      ImageView = require("view/ImageView"),
      AuthView = require("view/AuthView"),
      PreviewsView = require("view/PreviewsView"),
      TagPreviewsView = require("view/TagPreviewsView"),
      Image = require("model/Image"),
      ProfileView = require("view/ProfileView"),
      MainView = require("view/MainView"),
      TagInfoView = require("view/TagInfoView"),
      Tag = require("model/Tag")
      ;


  return MainView.extend({
    profileDropdownTemplate: _.template($('#profile-dropdown-tpl').html()),

    events: {
      'ajaxError': 'handleAjaxError',
      "imageUploaded": "refresh",
      "tagAdded": "refresh"
//      "error": 'handleAjaxError'
    },


    showProfile: function() {
      this.loadMainView(new ProfileView({model: window.galleryUser}));
    },


    showTagInfo: function(userId, tagId) {
      var tag = null;
      if(this.userTags != null){
        tag = this.userTags.get(tagId);
      } else {
        tag = new Tag({ownerId: userId, id: tagId});
        tag.fetch();
      }
      var info = new TagInfoView({model: tag});
      info.render()
    },


    handleAjaxError: function (event, request, settings, thrownError) {
      if(request.status == 401){
        window.galleryRouter.navigate("/auth", {trigger: true, replace: true});
      }
    },


    init: function () {
      console.log("init authorizing main view");
      $("#tags-menu-item").attr("href", "/users/" + window.galleryUser.id + "/tags");
      $("#auth-button").remove();
      var dropdown = this.profileDropdownTemplate(window.galleryUser.toJSON());
      $("#navbar-right").append(dropdown)
    }

  })
});
