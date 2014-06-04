/**
 * Created by alexey on 6/3/14.
 */
define(function(require){

  var $ = require("jquery"),
      ImagePreviewView = require("app/view/ImagePreviewView")
      ;



  return Backbone.View.extend({
    id: "image-previews",
    tagName: "div",
    className: "row images-gallery",

    initialize: function(){
      $("#main").html(this.el);
      this.initPopup();
      this.listenTo(this.collection, 'add', this.addImagePreview);
      this.listenTo(this.collection, 'reset', this.clearPreviews);
    },


    initPopup: function(){
      this.$el.magnificPopup({
        type: "image",
        delegate: '.image-link',
        tLoading: "Loading image #%curr%...",
        mainClass: "mfp-with-zoom",
        gallery: {
          enabled: true,
          navigateByImgClick: true,
          preload: [0,1]
        },
        image: {
          tError: '<a href="%url%">The image #%curr%</a> could not be loaded.'
        }
      });
    },


    addImagePreview: function(preview){
      var previewView = new ImagePreviewView({model: preview});
      this.$el.append(previewView.render().el);
    },


    clearPreviews: function(){
      this.$("#image-previews").empty();
    }
  });
});
