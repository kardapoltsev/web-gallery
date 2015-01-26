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
      $("#username").keyup(this.validateLogin.bind(this));
      $("#password").keyup(this.validatePassword.bind(this));
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
            200: function () {
              console.log("registration success");
              console.log("code 200");
              window.location = ("/");
            },
            400: function () {
              console.warn("user already exists");
            }
          }
        })
      });
    },


    isLoginValid: false,
    isPasswordValid: false,

    validateLogin: function () {
      var username = $("#username").val();
      console.log("validateLogin");
      if (username.length >= window.GalleryConfigs.MinLoginLength) {

        $.ajax({
          type: "GET",
          url: "api/validate/login/" + username,
          async: false,
          success: function (response){
            console.log(response);
            if (response.isValid){
              console.log(this);
              this.isLoginValid = true;
              $("#wrongLogin").hide();
            }
            else {

              this.isLoginValid = false;

              $("#wrongLogin").show();

            }
          }.bind(this)
        });
      }
      else {
        this.isLoginValid = false;
        $("#wrongLogin").hide();
      }
      this.checkLoginButtonEnabled();
    },


    disableLoginButton : function() {
      $("#sign-up").attr("disabled", "disabled");
    },


    validatePassword: function () {
      console.log("validate");
      if ($("#password").val().length >= window.GalleryConfigs.MinPasswordLength) {
        this.isPasswordValid = true;
      }
      else {
        this.isPasswordValid = false;
      }
      this.checkLoginButtonEnabled();
    },


    checkLoginButtonEnabled: function() {
      if(this.isLoginValid && this.isPasswordValid){
        $("#sign-up").removeAttr("disabled");
      }
      else {
        this.disableLoginButton();
      }
    },


    render: function () {
      $("#main").html(this.template());
      return this;
    }
  });

});
