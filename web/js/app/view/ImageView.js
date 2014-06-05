/**
 * Created by alexey on 6/4/14.
 */
define(function(require){

  var $ = require("jquery"),
      Backbone = require("backbone"),
      Tag = require("app/model/Tag")
      ;


  return Backbone.View.extend({
    rootUrl: "/images",
    id: "image-details",
    tagName: "div",
    className: "row",
    template: _.template($('#image-details-tpl').html()),

    events: {
      "click .btn-add-tag": "addTag"
    },

    initialize: function () {
      $("#main").html(this.el);
      this.render();
      this.listenTo(this.model, 'change', this.render);
    },


    addTag: function() {
      var tagName = this.$("#input-add-tag").val();
      var tag = new Tag();
      tag.set("name", tagName);
      console.log("adding tag " + tag);
      tag.save();
      this.model.set("tags", tag, {remove: false});
    },


    render: function() {
      this.$el.html(this.template(this.model.toJSON()));
      return this;
    }
  })
});
