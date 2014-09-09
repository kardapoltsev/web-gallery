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
      "images/:id": function(imageId){this.mainView.showImage(imageId)},
      "profile": function(){this.mainView.showProfile();},
      "users/:userId/tags": function(userId) {
        this.mainView.showTags(userId);
      },
      "users/:userId/tags/:tagId": function(userId, tagId) {
        console.warn("show tag info not implemented")
      }
    },


    index: function() {
      console.log("index")
    },


    showByTag: function (tagId) {
      this.mainView.showByTag(tagId);
    },


    initialize: function () {
    }

  });
});
