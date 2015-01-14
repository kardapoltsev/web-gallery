/**
 * Created by alexey on 6/3/14.
 */
define(function(require){

  var $ = require("jquery"),
      Backbone = require("backbone")
      ;

  return Backbone.View.extend({
    tagName: "div",
    className: "panel-group",
    id: function() {
      return "#comment-" + this.model.id;
    },
    parentEl: function() {
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
      this.listenTo(this.model, 'destroy', this.remove);
      this.render();
    },


    render: function() {
      console.log("render comment view");
      this.$el.html(this.template(this.model.toJSON()));
      $(this.parentEl()).append(this.$el);
      $("#reply-button-" + this.model.id).click(this.onReplyClick.bind(this));

      console.log(this.$el);
      //TODO: hide by default, show for owner
      if(this.model.get("ownerId") != window.galleryUser.id) {
        $("#delete-comment-" + this.model.id).hide();
      } else {
        $("#delete-comment-" + this.model.id).click(this.onDeleteClick.bind(this))
      }
      return this;
    },


    onDeleteClick: function() {
      console.log("deleting comment " + this.model.id);
      this.model.destroy();
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
