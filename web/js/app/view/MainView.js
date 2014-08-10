/**
 * Created by alexey on 6/4/14.
 */
define(function(require){

  var $ = require("jquery"),
      ImagePreviewList = require("app/collection/ImagePreviewList"),
      Sidebar = require("app/view/Sidebar"),
      Backbone = require("backbone"),
      ImageView = require("app/view/ImageView"),
      AuthView = require("app/view/AuthView"),
      PreviewsView = require("app/view/PreviewsView"),
      Image = require("app/model/Image")
      ;


  return Backbone.View.extend({
    id: "main-view",
    mainView: null,
    imagePreviews: null,
    sidebar: null,
    authView: null,

    events: {
      'ajaxError': 'handleAjaxError'
//      "error": 'handleAjaxError'
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


    handleAjaxError: function (event, request, settings, thrownError) {
      console.log(request)
      if(request.status == 401){
        if(!this.authView)
          this.authView = new AuthView();
        this.loadMainView(this.authView)
      }
    },


    initialize: function () {
      console.log("init main view");
      this.imagePreviews = new ImagePreviewList();
      this.sidebar = new Sidebar();
    },


    render: function() {

    },


    loadMainView : function(view) {
      this.mainView && this.mainView.remove();
      this.mainView = view;
    }

  })
});
