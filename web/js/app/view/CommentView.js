/**
 * Created by alexey on 6/3/14.
 */
define(function(require){

  var $ = require("jquery"),
      Backbone = require("backbone")
      ;

  return Backbone.View.extend({
    comments: null,
    el: function() {
      var parent = this.model.get("parentCommentId");
      console.log("parent comment id is " + parent);
      if(parent == this.model.id){
        return "#comments"
      } else {
        return "#comment-replies-" + parent;
      }
    },
    template: _.template($('#comment-tpl').html()),


    initialize: function() {
      console.log("init comment view");
      console.log(this.$el);
      this.listenTo(this.model, 'change', this.render);
      this.render();
    },


    render: function() {
      console.log("render comment view");
      console.log(this.model)
      this.$el.append(this.template(this.model.toJSON()));
      return this;
    }
  });
});
