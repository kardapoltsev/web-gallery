/**
 * Created by alexey on 6/3/14.
 */
require.config({

  baseUrl: "/js",
  waitSeconds: 10,
  paths: {
    app: "app",
    jquery: "lib/jquery/dist/jquery",
    "jquery-ui": "lib/jquery-ui/jquery-ui",
    "magnific-popup": "lib/magnific-popup/dist/jquery.magnific-popup",
    backbone: "lib/backbone/backbone",
    "backbone-relational": "lib/backbone-relational/backbone-relational",
    underscore: "lib/underscore/underscore",
    "bootstrap-tagsinput": "lib/bootstrap-tagsinput/dist/bootstrap-tagsinput",
    "bootstrap": "lib/bootstrap/dist/js/bootstrap",
    "file-upload": "lib/jquery-file-upload/js/jquery.fileupload",
    "iframe-transport": "lib/jquery-file-upload/js/jquery.iframe-transport",
    "jquery.ui.widget": "lib/jquery-ui/ui/widget",
    "typeahead": "lib/typeahead.js/dist/typeahead.jquery"
  },

  shim: {
    "jquery-ui": {
      exports: "$",
      deps: [
        "jquery"
      ]
    },
    "magnific-popup": {
      deps: [
        "jquery"
      ]
    },
    "backbone": {
      deps: ["underscore", "jquery"],
      exports: "Backbone"
    },
    "backbone-relational": {
      deps: ["underscore", "backbone"]
    },
    "underscore": {
      exports: "_"
    },
    "bootstrap-tagsinput": {
      deps: ["bootstrap", "typeahead"]
    },
    "typeahead": {
      deps: ["jquery"]
    },
    "bootstrap": {
      deps: ["jquery"]
    },
    "file-upload": {
      deps: ["iframe-transport"]
    }
  }
});


require(
    ["jquery", "backbone", "router", "magnific-popup", "jquery-ui", "bootstrap-tagsinput", "file-upload"],
    function ($, Backbone, Router) {

      $("#input-search-tags").typeahead({
        hint: true,
        highlight: true,
        minLength: 1
      },
      {
        name: "tags",
        displayKey: "name",
        source: function (q, cb){
          var url = "/api/search/tags?term=" + q;
          $.get(url).done(function(data){
            cb(data.tags);
          }).fail(function(){
            cb([])
          })
        }
      }).bind("typeahead:selected", function (e, item){
        window.location = "/images?tagId=" + item.id;
      });


      $('#upload-images').fileupload({
        dataType: 'json',
        start: function(e){
          $('#progress').css(
              'opacity', 100
          );
        },
        stop: function (e) {
          $('#progress').css(
              'opacity', 0
          );
          $(document).trigger("imageUploaded")
        },
        progressall: function (e, data) {
          var progress = parseInt(data.loaded / data.total * 100, 10);
          $('#progress .bar').css(
              'width', progress + '%'
          );
        }
    });


      window.galleryRouter = new Router();


      $(document.body).on('click', 'a:not([direct-link])', function(e){
        e.preventDefault();
        var url = e.currentTarget.pathname + e.currentTarget.search;
        Backbone.history.navigate(url, {trigger: true});
      });
    }
);
