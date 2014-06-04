/**
 * Created by alexey on 6/3/14.
 */
require.config({

  baseUrl: "js/lib",
  waitSeconds: 2,
  paths: {
    app: "../app",
    tpl: "../tpl",
    jquery: "jquery/dist/jquery",
    "jquery-ui": "jquery-ui/ui/jquery-ui",
    "magnific-popup": "magnific-popup/dist/jquery.magnific-popup",
    backbone: "backbone/backbone",
    underscore: "underscore/underscore"
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
    "underscore": {
      exports: "_"
    }
  }
});


require(
    ["jquery", "backbone", "app/router", "magnific-popup", "jquery-ui"],
    function ($, Backbone, Router) {


      $("#input-search-tags").autocomplete({
        source: "/search/tags",
        select: function (event, ui) {
          window.location = "/#images?tag=" + ui.item.value;
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
