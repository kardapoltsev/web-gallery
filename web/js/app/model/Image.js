/**
 * Created by alexey on 6/3/14.
 */
define(function(require){

  var Backbone = require("backbone"),
      BackboneRelational = require("backbone-relational"),
      Tag = require("app/model/Tag")
      ;

  return Backbone.RelationalModel.extend({
    urlRoot: "/api/images/",
    relations:[{
      type: Backbone.HasMany,
      key: "tags",
      relatedModel: Tag
    }]
  });
});
