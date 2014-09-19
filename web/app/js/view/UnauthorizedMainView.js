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
      MainView = require("view/MainView"),
      ProfileView = require("view/ProfileView")
      ;


  return MainView.extend({
    id: "main-view",
    authView: null,

    auth: function() {
      console.log("show auth dialog");
      if(!this.authView)
        this.authView = new AuthView();
      this.loadMainView(this.authView)
    }
  })
});
