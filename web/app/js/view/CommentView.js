/**
 * Created by alexey on 6/3/14.
 */
define(function(require){

  var $ = require("jquery"),
      Backbone = require("backbone")
      ;

  return Backbone.View.extend({
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
      this.listenTo(this.model, 'change', this.render);
      this.render();
    },


    render: function() {
      console.log("render comment view");
      this.$el.append(this.template(this.model.toJSON()));
      $("#reply-button-" + this.model.id).click(this.onReplyClick.bind(this));
      return this;
    },


    onReplyClick: function() {
      console.log("onReplyClick");
      var textarea = $("#reply-text-" + this.model.id);
      var text = textarea.val();
      textarea.val("");
      this.trigger("reply-added", this.model, {text: text})
    }
  });
});
