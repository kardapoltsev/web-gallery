/**
 * Created by alexey on 6/4/14.
 */
define(function(require){

  var $ = require("jquery"),
      Backbone = require("backbone"),
      MainView = require("view/MainView"),
      AuthView = require("view/AuthView"),
      RegistrationView = require("view/RegistrationView")
      ;


  return MainView.extend({
    authView: null,
    registrationView: null,


    init: function(){
      this.initAuth();
    },


    initAuth: function() {
      //TODO: validation
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
              $("#wrongLoginPassword").show();
            }

          }
        })
      });
    },


//    auth: function() {
//      console.log("show auth dialog");
////      if(!this.authView)
//        this.authView = new AuthView();
//      this.loadMainView(this.authView)
//    },


    registration: function() {
      console.log("show register dialog");
//      if(!this.registrationView)
        this.registrationView = new RegistrationView();
      this.loadMainView(this.registrationView)
    }
  })
});
