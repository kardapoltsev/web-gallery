/**
 * Created by alexey on 6/3/14.
 */
define(function(require){

  var Backbone = require("backbone"),
      AuthorizedMainView = require("view/AuthorizedMainView"),
      UnauthorizedMainView = require("view/UnauthorizedMainView"),
      User = require("model/User")
      ;


  return Backbone.Router.extend({
    mainView: null,
    routes: {
      "": "index",
      "auth": function(){this.mainView.auth();},
      "registration": function(){this.mainView.registration();},
      "images?tagId=:id": function(tagId){this.mainView.showByTag(tagId);},
      "images/popular": function(){this.mainView.showPopular();},
      "images/:id": function(imageId){this.mainView.showImage(imageId);},
      "profile": function(){this.mainView.showProfile();},
      "users/:userId": function(userId) {this.mainView.showUser(userId);},
      "users/:userId/tags": function(userId) {this.mainView.showTags(userId);},
      "users/:userId/tags/:tagId": function(userId, tagId) {this.mainView.showTagInfo(userId, tagId);},
      "stats": function(){this.mainView.showStats();}
    },


    index: function() {
      console.log("index")
    },


    initialize: function () {
      var user = new User({id: "current"});
      var req = user.fetch({async: false, context: this});
      req.fail(function(r, status, error){
        console.log("get user request failed");
        if(r.status == 401){
          console.log("401 err in router init");
          this.mainView = new UnauthorizedMainView({el: document});
          Backbone.history.start({pushState: true});
          this.navigate("/images/popular", {trigger: true, replace: true});
        }
      });
      req.success(function(){
        console.log("got user, creating main view");
        console.log(user.toJSON());
        window.galleryUser = user;
        this.mainView = new AuthorizedMainView({el: document});
        Backbone.history.start({pushState: true});
      });
    }

  });
});
