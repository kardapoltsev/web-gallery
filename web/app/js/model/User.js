/**
 * Created by alexey on 6/3/14.
 */
define(function(require){

  var Backbone = require("backbone")
      ;

  return Backbone.Model.extend({
    urlRoot: "/api/users/",
    parse: function (response) {
      if(typeof response.user != "undefined"){
        return response.user;
      } else {
        return response;
      }
    }
  });
});
