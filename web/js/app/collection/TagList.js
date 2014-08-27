/**
 * Created by alexey on 6/3/14.
 */
define(function(require){

  var Backbone = require("backbone"),
      Tag = require("app/model/Tag");

  return Backbone.Collection.extend({
    model: Tag,
    url: function(){
      return "/api/users/" + window.galleryUser.id + "/tags/recent";
    },
    parse: function(response) {
      return response.tags;
    }
  });
});
