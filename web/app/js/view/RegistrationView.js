/**
 * Created by alexey on 6/3/14.
 */
define(function (require) {

  var $ = require("jquery"),
      Backbone = require("backbone")
      ;

  return Backbone.View.extend({
    id: "registration-view",
    template: _.template($('#registration-tpl').html()),

    events: {
    },


    initialize: function () {
      console.log("init register view");
      this.render();
      $("#sign-up").click(function () {
        console.log("registration started");
        var username = $("#username").val();
        var name = $("#name").val();
        var password = $("#password").val();
        var data = {
          "authId": username,
          "name": name,
          "authType": "Direct",
          "password": password
        };

        $.ajax({
          type: "POST",
          url: "/api/users",
          data: JSON.stringify(data),
          processData: false,
          async: true,
          dataType: "json",
          contentType: 'application/json; charset=UTF-8',
          statusCode: {
            200: function() {
              console.log("registration success");
              console.log("code 200");
              window.location = ("/");
            },
            400: function() {
              console.warn("user already exists");
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
