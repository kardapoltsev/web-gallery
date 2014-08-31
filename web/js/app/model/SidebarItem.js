/**
 * Created by alexey on 6/3/14.
 */
define(function(require){

  var Backbone = require("backbone"),
      BackboneRelational = require("backbone-relational")
      ;

  return Backbone.RelationalModel.extend({
    urlRoot: function () {
      return "/api/users/" + window.galleryUser.id + "/tags"
    },
    defaults: function(){
      return {
        id: null,
        ownerId: null,
        name: null
      }
    }
  });
});
