/**
 * Created by alexey on 6/3/14.
 */
define(function(require){

  var Backbone = require("backbone")
      ;

  return Backbone.Model.extend({
    imageId: null,
    urlRoot: function () {
      return "/api/images/" + this.imageId + "/comments"
    },
    initialize: function(options){
      console.log("initialize comment");
      this.imageId = options.imageId;
    },
    parse: function(response){
      console.log("parsing in model");
      if(typeof response.comment == "undefined"){
        return response
      } else {
        return response.comment
      }
    }
  });
});
