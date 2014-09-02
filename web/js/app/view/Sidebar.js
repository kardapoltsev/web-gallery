/**
 * Created by alexey on 6/3/14.
 */
define(function(require){

  var $ = require("jquery"),
      Backbone = require("backbone"),
      TagView = require("app/view/TagView"),
      RecentTagList = require("app/collection/RecentTagList")
      ;



  return Backbone.View.extend({
    el: $("#web-gallery"),
    tags: new RecentTagList(),

    initialize: function() {
      console.log("init sidebar");
      this.listenTo(this.tags, 'add', this.addTag);
      this.tags.fetch();
    },


    addTag: function(tag){
      var tagView = new TagView({model: tag});
      this.$("#sidebar").append(tagView.render().el);
    }
  });
});
