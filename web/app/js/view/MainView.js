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
    tagsView: null,
    userTags: null,

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


    showPopular: function() {
      console.log("show popular tags");
      this.loadMainView(new PreviewsView({collection: this.imagePreviews}));
      //TODO: fetch with reset, render view on init
      this.imagePreviews.reset();
      this.imagePreviews.url = "/api/images/popular";
      this.imagePreviews.fetch();
    },


    showByTag: function (tagId) {
      console.log("show by tag " + tagId);
      this.loadMainView(new PreviewsView({collection: this.imagePreviews}));
      //TODO: fetch with reset, render view on init
      this.imagePreviews.reset();
//      this.imagePreviews.url = "/api/images?tagId=" + tagId;
      this.imagePreviews.baseUrl = "/api/images";
      this.imagePreviews.query = "tagId=" + tagId;
      this.imagePreviews.fetch();
    },


    showTags: function(userId) {
      console.log("show tags for user " + userId);
      this.userTags = new UserTagsList({userId: userId});
      this.tagsView = new TagPreviewsView({collection: this.userTags});
      this.userTags.fetch();
    },


    initialize: function () {
      console.log("initialize main view");
      this.imagePreviews = new ImagePreviewList();
      this.init();
    },


    //used to initialize subclasses
    init: function(){

    },


    loadMainView : function(view) {
      this.mainView && this.mainView.remove();
      this.mainView = view;
    }

  })
});
