/**
 * Created by alexey on 6/3/14.
 */
define(function(require){

  var $ = require("jquery"),
      Backbone = require("backbone")
      ;

  return Backbone.View.extend({

    el: '#main',
    template: _.template($('#stats-tpl').html()),

    events: {
    },


    initialize: function() {
      console.log("init stats view");
      this.render();
      this.listenTo(this.model, 'sync', this.render);
    },


    render: function() {
      this.$el.html(this.template(this.model.toJSON()));
      return this;
    }
  });
});
