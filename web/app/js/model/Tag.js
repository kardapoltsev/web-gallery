/**
 * Created by alexey on 6/3/14.
 */
define(function(require){

  var Backbone = require("backbone"),
      BackboneRelational = require("backbone-relational")
      ;

  return Backbone.RelationalModel.extend({
    urlRoot: function () {
      return "/api/users/" + this.get("ownerId") + "/tags"
    },


    defaults: function(){
      return {
        id: null,
        ownerId: null,
        name: null
      }
    },


    parse: function(response) {
      console.log("parsing Tag from");
      console.log(response);
      if(typeof response.tag != "undefined") {
        return response.tag;
      } else {
        return response;
      }
    },


    initialize: function() {
    }

  });
});
