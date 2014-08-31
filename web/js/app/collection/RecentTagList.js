/**
 * Created by alexey on 6/3/14.
 */
define(function(require){

  var Backbone = require("backbone"),
      SidebarItem = require("app/model/SidebarItem");

  return Backbone.Collection.extend({
    model: SidebarItem,
    url: function(){
      return "/api/users/" + window.galleryUser.id + "/tags/recent";
    },
    parse: function(response) {
      return response.tags;
    }
  });
});
