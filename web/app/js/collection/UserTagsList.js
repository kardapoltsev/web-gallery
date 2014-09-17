/**
 * Created by alexey on 6/3/14.
 */
define(function(require){

  var Backbone = require("backbone"),
      Tag = require("model/Tag");

  return Backbone.Collection.extend({
    model: Tag,
    userId: null,


    url: function() {
      return "/api/users/" + this.userId + "/tags";
    },


    parse: function(response) {
      return response.tags;
    },


    initialize: function(args) {
      console.log("initialize user tags list");
      this.userId = args.userId;
    }

  });
});
