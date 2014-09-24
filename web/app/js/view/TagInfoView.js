/**
 * Created by alexey on 6/3/14.
 */
define(function(require){

  var $ = require("jquery"),
      Backbone = require("backbone"),
      GranteesList = require("collection/GranteesList"),
      UserPreviewView = require("view/UserPreviewView"),
      User = require("model/User")
      ;

  return Backbone.View.extend({
    el: '#main',
    grantees: null,
    template: _.template($('#tag-info-tpl').html()),

    initialize: function() {
      console.log("initialize TagInfoView");
      this.listenTo(this.model, 'change', this.render);
      this.grantees = new GranteesList([], {tagId: this.model.id});
      this.listenTo(this.grantees, 'add', this.addGrantee);
      this.listenTo(this.grantees, 'reset', this.clearGrantees);
      this.grantees.fetch();
      console.log($("#user-search"))
    },

    addGrantee: function(grantee){
      console.log("add grantee:");
      console.log(grantee)
      var granteeView = new UserPreviewView({model: grantee});
      $("#tag-grantees").append(granteeView.render().el);
    },


    clearGrantees: function(){
      this.$("#tag-grantees").empty();
    },

    render: function() {
      this.$el.html(this.template(this.model.toJSON()));
      this.initUserSearch();
      return this;
    },


    initUserSearch: function() {
      var ta = $("#user-search").typeahead({
            hint: true,
            highlight: true,
            minLength: 1
          },
          {
            name: "users",
            displayKey: "name",
            source: function (q, cb){
              var url = "/api/search/users?term=" + q;
              $.get(url).done(function(data){
                cb(data.users);
              }).fail(function(){
                cb([])
              })
            }
          })

      ta.on("typeahead:selected", function (e, item){
        $.ajax("/api/acl/tag/" + this.model.id,
             {
               data: JSON.stringify([item.id]),
               type: "PUT",
               processData: false,
               async: false,
               dataType: "json",
               contentType: 'application/json; charset=UTF-8',
               success: function(){
               },
               context: this
             });
        console.log("adding grantee")
        this.grantees.fetch();
      }.bind(this));

    }
  });
});
