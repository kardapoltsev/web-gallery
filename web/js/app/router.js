/**
 * Created by alexey on 6/3/14.
 */
define(function(require){

  var Backbone = require("backbone"),
      ImagePreviewList = require("app/collection/ImagePreviewList"),
      Sidebar = require("app/view/Sidebar"),
      PreviewsView = require("app/view/PreviewsView"),
      ImageView = require("app/view/ImageView"),
      Image = require("app/model/Image")
      ;

  var imagePreviews = new ImagePreviewList();
  var sidebar = new Sidebar();


  var GeneralErrorView = Backbone.View.extend({
    events: {
      'ajaxError': 'handleAjaxError'
    },
    handleAjaxError: function (event, request, settings, thrownError) {
      console.log("ajax error")
      //TODO: handle Unauthorized error, create one main app view
      console.log(event)
    }
  });

  var mainView = new GeneralErrorView({el: document});


  return Backbone.Router.extend({
    mainView: null,
    routes: {
      "": "index",
      "images?tagId=:id": "showByTag",
      "images/:id": "showImage"
    },


    index: function() {
      console.log("index")
    },


    showImage: function (id) {
      var image = imagePreviews.get(id);
      if(typeof image == "undefined"){
        console.log("fetching image");
        image = new Image();
        image.set("id", id);
        image.fetch({async: false});
      }
      this.loadMainView(new ImageView({model: image}));
    },


    showByTag: function (tagId) {
      this.loadMainView(new PreviewsView({collection: imagePreviews}));
      //TODO: fetch with reset, render view on init
      imagePreviews.reset();
      imagePreviews.url = "/api/images?tagId=" + tagId;
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
