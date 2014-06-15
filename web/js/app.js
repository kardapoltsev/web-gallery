/**
 * Created by alexey on 6/3/14.
 */
require.config({

  baseUrl: "/js/lib",
  waitSeconds: 2,
  paths: {
    app: "../app",
    tpl: "../tpl",
    jquery: "jquery/dist/jquery",
    "jquery-ui": "jquery-ui/ui/jquery-ui",
    "magnific-popup": "magnific-popup/dist/jquery.magnific-popup",
    backbone: "backbone/backbone",
    "backbone-relational": "backbone-relational/backbone-relational",
    underscore: "underscore/underscore",
    "bootstrap-tagsinput": "bootstrap-tagsinput/dist/bootstrap-tagsinput",
    "bootstrap": "bootstrap/dist/js/bootstrap",
    "file-upload": "jquery-file-upload/js/jquery.fileupload",
    "iframe-transport": "jquery-file-upload/js/jquery.iframe-transport",
    "jquery.ui.widget": "jquery-ui/ui/jquery.ui.widget"
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
      deps: ["backbone"]
    },
    "underscore": {
      exports: "_"
    },
    "bootstrap-tagsinput": {
      deps: ["bootstrap"]
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
    ["jquery", "backbone", "app/router", "magnific-popup", "jquery-ui", "bootstrap-tagsinput", "file-upload"],
    function ($, Backbone, Router) {


      $("#input-search-tags").autocomplete({
        source: "/search/tags",
        select: function (event, ui) {
          window.location = "/images?tag=" + ui.item.value;
        }
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
        },
        progressall: function (e, data) {
          var progress = parseInt(data.loaded / data.total * 100, 10);
          $('#progress .bar').css(
              'width', progress + '%'
          );
        }
    });


      new Router();
      Backbone.history.start({pushState: true});


      $(document.body).on('click', 'a:not([direct-link])', function(e){
        e.preventDefault();
        var url = e.currentTarget.pathname + e.currentTarget.search;
        Backbone.history.navigate(url, {trigger: true});
      });
    }
);
