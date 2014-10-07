/**
 * Created by alexey on 6/3/14.
 */
define(function(require){

  var $ = require("jquery"),
      ImagePreviewView = require("view/ImagePreviewView")
      ;



  return Backbone.View.extend({
    id: "image-previews",
    tagName: "div",
    className: "row images-gallery",

    initialize: function(){
      console.log("initializing previews view");
      $("#main").html(this.el);
      this.initPopup();
      this.initScrollListener();
      this.listenTo(this.collection, 'add', this.addImagePreview);
      this.listenTo(this.collection, 'reset', this.clearPreviews);
//      this.listenTo(this.collection, 'sync', this.checkScreenFull);
      this.initScrollListener();
    },


    checkScreenFull: function(){
      if($(window).scrollTop() == $(document).height() - $(window).height()) {
        console.log("already at bottom");
        this.collection.loadMore();
      }
    },


    initScrollListener: function() {
      $(window).scroll(function() {
        if($(window).scrollTop() == $(document).height() - $(window).height()) {
          console.log("scrolled to bottom")
          this.collection.loadMore();
        }
      }.bind(this));
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
        },
        callbacks: {
          open: function() {
            history.back();
          }
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
