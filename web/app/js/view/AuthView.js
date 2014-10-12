/**
 * Created by alexey on 6/3/14.
 */
define(function (require) {

  var $ = require("jquery"),
      Backbone = require("backbone")
      ;

  return Backbone.View.extend({
    id: "auth-view",
    template: _.template($('#auth-tpl').html()),

    events: {
    },


    initialize: function () {
      console.log("init auth view");
      this.render();
      $("#registration").click(function(){
        window.galleryRouter.navigate("/registration", {trigger: true});
      });
      $("#sign-in").click(function () {
        console.log("authorizing");
        var username = $("#username").val();
        var password = $("#password").val();
        var data = {
          "authId": username,
          "authType": "Direct",
          "password": password
        };

        $.ajax({
          type: "POST",
          url: "/api/auth",
          data: JSON.stringify(data),
          processData: false,
          async: true,
          dataType: "json",
          contentType: 'application/json; charset=UTF-8',
          statusCode: {
            200: function() {
              console.log("auth success");
              console.log("code 200");
              window.location = ("/");
            },
            404: function() {
              console.warn("wrong credentials");
            }
          }
        })
      });
    },


    render: function () {
      $("#main").html(this.template());
      return this;
    }
  });
});
