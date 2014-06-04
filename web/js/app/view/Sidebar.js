/**
 * Created by alexey on 6/3/14.
 */
define(function(require){

  var $ = require("jquery"),
      Backbone = require("backbone"),
      TagView = require("app/view/TagView"),
      TagList = require("app/collection/TagList")
      ;

  var tags = new TagList();


  return Backbone.View.extend({
    el: $("#web-gallery"),

    initialize: function(){
      this.listenTo(tags, 'add', this.addTag);
      tags.fetch();
    },


    addTag: function(tag){
      var tagView = new TagView({model: tag});
      this.$("#sidebar").append(tagView.render().el);
    }
  });
});
