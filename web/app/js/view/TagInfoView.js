/**
 * Created by alexey on 6/3/14.
 */
define(function(require){

  var $ = require("jquery"),
      Backbone = require("backbone"),
      GranteesList = require("collection/GranteesList"),
      GranteesListItemView = require("view/GranteesListItemView"),
      User = require("model/User")
      ;

  return Backbone.View.extend({
    AnonymousUserId: 2,
    el: '#main',
    grantees: null,
    template: _.template($('#tag-info-tpl').html()),

    initialize: function() {
      console.log("initialize TagInfoView");
      this.listenTo(this.model, 'change', this.render);
      this.grantees = new GranteesList([], {tagId: this.model.id});
      this.listenTo(this.grantees, 'add', this.addGranteeView);
      this.listenTo(this.grantees, 'remove', this.removeGranteeView);
      this.listenTo(this.grantees, 'reset', this.clearGrantees);
      this.grantees.fetch();

    },


    addGranteeView: function(grantee){
      var granteeView = new GranteesListItemView({model: grantee});
      $("#tag-grantees").append(granteeView.render().el);
      $("#remove-grantee-" + grantee.id).click(function(){
         this.deleteGrantee(grantee.id);
      }.bind(this));
    },


    removeGranteeView: function(grantee){
      console.log("removing grantee view for " + grantee.id);
      $("#tag-grantee-" + grantee.id).remove();
    },


    clearGrantees: function(){
      this.$("#tag-grantees").empty();
    },


    render: function() {
      this.$el.html(this.template(this.model.toJSON()));
      this.initUserSearch();
      this.initPublicCheckboxListener();
      return this;
    },


    initPublicCheckboxListener: function(){
      $("#public-checkbox").click(function(){
        var isPublic = $("#public-checkbox").is(":checked");
        console.log("public clicked. isPublic: " + isPublic);
        if(isPublic) {
          this.addGrantee(this.AnonymousUserId);
          $("#acl-management").hide();
        } else {
          this.deleteGrantee(this.AnonymousUserId);
          $("#acl-management").show();
        }
      }.bind(this))
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
          });

      ta.on("typeahead:selected", function (e, item){
        this.addGrantee(item.id)
      }.bind(this));

    },


    addGrantee: function(userId) {
      $.ajax("/api/acl/tag/" + this.model.id,
          {
            data: JSON.stringify([userId]),
            type: "PUT",
            processData: false,
            async: false,
            dataType: "json",
            contentType: 'application/json; charset=UTF-8',
            success: function(){
            },
            context: this
          });
      console.log("adding grantee " + userId);
      this.grantees.fetch();
    },


    deleteGrantee: function(userId) {
      $.ajax("/api/acl/tag/" + this.model.id,
          {
            data: JSON.stringify([userId]),
            type: "DELETE",
            processData: false,
            async: false,
            dataType: "json",
            contentType: 'application/json; charset=UTF-8',
            success: function(){
            },
            context: this
          });
      console.log("deleting grantee " + userId);
      var g = this.grantees.get(userId);
      this.grantees.remove(g);
    }
  });

});
