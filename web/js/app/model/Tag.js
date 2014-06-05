/**
 * Created by alexey on 6/3/14.
 */
define(function(require){

  var Backbone = require("backbone"),
      BackboneRelational = require("backbone-relational")
      ;

  return Backbone.RelationalModel.extend({
    urlRoot: "/api/tags",
    idAttribute: "name",
    defaults: function(){
      return {
        id: null,
        name: null
      }
    }
  });
});
