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

    auth: function() {
      console.log("show auth dialog");
//      if(!this.authView)
        this.authView = new AuthView();
      this.loadMainView(this.authView)
    },


    registration: function() {
      console.log("show register dialog");
//      if(!this.registrationView)
        this.registrationView = new RegistrationView();
      this.loadMainView(this.registrationView)
    }
  })
});
