/**
 * Created by alexey on 6/3/14.
 */
define(function(require){

  var $ = require("jquery"),
      Backbone = require("backbone"),
      Comment = require("app/model/Comment")
      ;

  return Backbone.Collection.extend({
    model: Comment,
    imageId: null,
    initialize: function(models, options) {
      console.log(options);
      this.imageId = options.imageId;
      return this;
    },
    url: function(){
      return "/api/images/" + this.imageId + "/comments";
    },
    parse: function(response) {
      console.log("parsing in collection")
      return response.comments;
    }
  });
});
