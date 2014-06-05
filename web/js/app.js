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
    "bootstrap": "bootstrap/dist/js/bootstrap"
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
    }
  }
});


require(
    ["jquery", "backbone", "app/router", "magnific-popup", "jquery-ui", "bootstrap-tagsinput"],
    function ($, Backbone, Router) {


      $("#input-search-tags").autocomplete({
        source: "/search/tags",
        select: function (event, ui) {
          window.location = "/images?tag=" + ui.item.value;
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
