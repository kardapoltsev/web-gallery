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
      this.listenTo(this.model, 'sync', this.render);
    },


    render: function() {
      console.log("rendering profile view")
      this.$el.html(this.template(this.model.toJSON()));
      $('#upload-avatar').fileupload({
        start: function(e) {
          console.log("start uploading user avatar")
        },
        stop: function (e) {
          console.log("finish uploading user avatar")
          $('#upload-avatar').attr("disabled", "disabled")
          window.galleryUser.fetch();
        }
      });
      return this;
    }
  });
});
