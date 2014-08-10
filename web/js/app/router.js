/**
 * Created by alexey on 6/3/14.
 */
define(function(require){

  var Backbone = require("backbone"),
      MainView = require("app/view/MainView")
      ;


  return Backbone.Router.extend({
    mainView: new MainView({el: document}),
    routes: {
      "": "index",
      "images?tagId=:id": "showByTag",
      "images/:id": "showImage"
    },


    index: function() {
      console.log("index")
    },


    showImage: function (id) {
      this.mainView.showImage(id);
    },


    showByTag: function (tagId) {
      this.mainView.showByTag(tagId);
    },


    initialize: function () {
    }

  });
});
