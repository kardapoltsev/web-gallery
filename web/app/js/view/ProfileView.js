/**
 * Created by alexey on 6/3/14.
 */
define(function(require){

  var $ = require("jquery"),
      Backbone = require("backbone")
      ;

  return Backbone.View.extend({

    el: '#main',
    template: _.template($('#profile-tpl').html()),

    events: {
    },


    initialize: function() {
      console.log("init profile view");
      this.render();
    },


    render: function() {
      this.$el.html(this.template(window.galleryUser.toJSON()));
      return this;
    }
  });
});
