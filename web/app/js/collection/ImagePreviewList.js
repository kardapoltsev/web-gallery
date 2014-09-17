/**
 * Created by alexey on 6/3/14.
 */
define(function(require){

  var $ = require("jquery"),
      Backbone = require("backbone"),
      Image = require("model/Image")
      ;

  return Backbone.Collection.extend({
    model: Image,
    url: '/tags',
    parse: function (response) {
      return response.images
    }
  });
});
