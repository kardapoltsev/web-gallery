/**
 * Created by alexey on 6/3/14.
 */
define(function(require){

  var $ = require("jquery"),
      Backbone = require("backbone")
      ;

  return Backbone.View.extend({

    template: _.template($('#grantees-list-item-tpl').html()),

    initialize: function() {
      console.log("init user preview view");
      this.render();
    },


    render: function() {
      this.$el.html(this.template(this.model.toJSON()));
      return this;
    }
  });
});
