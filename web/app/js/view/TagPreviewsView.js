/**
 * Created by alexey on 6/3/14.
 */
define(function(require){

  var $ = require("jquery"),
      TagView = require("view/TagView")
      ;



  return Backbone.View.extend({
    id: "tag-previews",
    tagName: "div",
    className: "row images-gallery",

    initialize: function(){
      $("#main").html(this.el);
      this.listenTo(this.collection, 'add', this.addTag);
      this.listenTo(this.collection, 'reset', this.clearTags);
    },


    addTag: function(tag){
      console.log("adding tag");
      var tagView = new TagView({model: tag});
      this.$el.append(tagView.render().el);
    },


    clearTags: function(){
      this.$("#tags-previews").empty();
    }
  });
});
