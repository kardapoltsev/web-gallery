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


    auth: function() {
      console.log("show auth dialog");
      if(!this.authView)
        this.authView = new AuthView();
      this.loadMainView(this.authView)
    },


    initialize: function () {
      console.log("init unauthorized main view");
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
