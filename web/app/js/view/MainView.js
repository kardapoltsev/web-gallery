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
      User = require("model/User"),
      ProfileView = require("view/ProfileView")
      ;


  return Backbone.View.extend({
    id: "main-view",
    mainView: null,
    imagePreviews: null,
    authView: null,
    tagsView: null,
    userTags: null,

    events: {
      'ajaxError': 'handleAjaxError',
      "imageUploaded": "refresh",
      "tagAdded": "refresh"
//      "error": 'handleAjaxError'
    },


    showProfile: function() {
      this.loadMainView(new ProfileView());
    },


    showImage: function (id) {
      var image = this.imagePreviews.get(id);
      if(typeof image == "undefined"){
        console.log("fetching image");
        image = new Image();
        image.set("id", id);
        image.fetch({async: false});
      }
      this.loadMainView(new ImageView({model: image}));
    },


    showByTag: function (tagId) {
      this.loadMainView(new PreviewsView({collection: this.imagePreviews}));
      //TODO: fetch with reset, render view on init
      this.imagePreviews.reset();
      this.imagePreviews.url = "/api/images?tagId=" + tagId;
      this.imagePreviews.fetch();
    },

    showTags: function(userId) {
      console.log("show tags for user " + userId);
      this.userTags = new UserTagsList({userId: userId});
      this.tagsView = new TagPreviewsView({collection: this.userTags});
      this.userTags.fetch();
    },

    handleAjaxError: function (event, request, settings, thrownError) {
      if(request.status == 401){
        if(!this.authView)
          this.authView = new AuthView();
        this.loadMainView(this.authView)
      }
    },


    initialize: function () {
      console.log("init main view");
      var user = new User({id: "current"});
      var req = user.fetch({async: false, context: this});
      req.fail(function(r, status, error){
        console.log("get user request failed");
        if(r.status == 401){
          console.log("show auth dialog");
          if(!this.authView)
            this.authView = new AuthView();
          this.loadMainView(this.authView)
        }
      });
      window.galleryUser = user;
      $("#tags-menu-item").attr("href", "/users/" + user.id + "/tags");
      req.success(function(){
        console.log("got user, creating main view");
        console.log(user.toJSON());
        this.imagePreviews = new ImagePreviewList();
      });
    },


    render: function() {

    },


    refresh: function(){
      console.log("refreshing");
    },


    loadMainView : function(view) {
      this.mainView && this.mainView.remove();
      this.mainView = view;
    }

  })
});
