/**
 * Created by alexey on 6/3/14.
 */
define(function(require){

  var Backbone = require("backbone")
      ;

  return Backbone.Model.extend({
    urlRoot: "/api/images/"
  });
});
