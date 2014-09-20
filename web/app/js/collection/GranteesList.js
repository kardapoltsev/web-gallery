/**
 * Created by alexey on 6/3/14.
 */
define(function(require){

  var $ = require("jquery"),
      Backbone = require("backbone"),
      User = require("model/User")
      ;

  return Backbone.Collection.extend({
    model: User,
    tagId: null,
    initialize: function(models, options) {
      console.log(options);
      this.tagId = options.tagId;
      return this;
    },
    url: function(){
      return "/api/acl/tag/" + this.tagId;
    },
    parse: function(response) {
      console.log("parsing in collection grantees");
      return response.users;
    }
  });
});
