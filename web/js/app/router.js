/**
 * Created by alexey on 6/3/14.
 */
define(function(require){

  var Backbone = require("backbone"),
      ImagePreviewList = require("app/collection/ImagePreviewList"),
      Sidebar = require("app/view/Sidebar"),
      PreviewsView = require("app/view/PreviewsView"),
      ImageView = require("app/view/ImageView")
      ;

  var imagePreviews = new ImagePreviewList();
  var sidebar = new Sidebar();

  return Backbone.Router.extend({
    mainView: null,
    routes: {
      "": "index",
      "images?tag=:name": "showByTag",
      "images/:id": "showImage"
    },


    index: function() {
      console.log("index")
    },


    showImage: function (id) {
      var image = imagePreviews.get(id);
      this.loadMainView(new ImageView({model: image}));
    },


    showByTag: function (tagName) {
      this.loadMainView(new PreviewsView({collection: imagePreviews}));
      //TODO: fetch with reset, render view on init
      imagePreviews.reset();
      imagePreviews.url = "/api/images?tag=" + tagName;
      imagePreviews.fetch();
    },


    loadMainView : function(view) {
      this.mainView && this.mainView.remove();
      this.mainView = view;
    },


    initialize: function () {
    }
  });
});
