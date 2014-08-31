/**
 * Created by alexey on 6/3/14.
 */
define(function(require){

  var $ = require("jquery"),
      Backbone = require("backbone"),
      Tag = require("app/model/Tag")
      ;

  return Backbone.Collection.extend({
    model: Tag
  });
});
