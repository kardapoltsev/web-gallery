/**
 * Created by alexey on 6/3/14.
 */
define(function(require){

  var $ = require("jquery"),
      Backbone = require("backbone")
      ;

  return Backbone.View.extend({

    tagName:  "div",
    className :"col-xs-6 col-sm-3 col-md-2",

    template: _.template($('#image-preview-tpl').html()),


    initialize: function() {
      this.listenTo(this.model, 'change', this.render);
    },


    render: function() {
      this.$el.html(this.template(this.model.toJSON()));
      return this;
    }
  });
});
