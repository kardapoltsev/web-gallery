/**
 * Created by alexey on 6/3/14.
 */
define(function(require){

  var $ = require("jquery"),
      Backbone = require("backbone"),
      GranteesList = require("collection/GranteesList"),
      UserPreviewView = require("view/UserPreviewView")
      ;

  return Backbone.View.extend({
    el: '#main',
    grantees: null,
    template: _.template($('#tag-info-tpl').html()),

    initialize: function() {
      this.listenTo(this.model, 'change', this.render);
      this.grantees = new GranteesList([], {tagId: this.model.id});
      this.listenTo(this.grantees, 'add', this.addGrantee);
      this.listenTo(this.grantees, 'reset', this.clearGrantees);
      this.grantees.fetch();
    },

    addGrantee: function(grantee){
      console.log("add grantee");
      var granteeView = new UserPreviewView({model: grantee});
      this.$("#tag-grantees").append(granteeView.render().el);
    },


    clearGrantees: function(){
      this.$("#tag-grantees").empty();
    },

    render: function() {
      this.$el.html(this.template(this.model.toJSON()));
      return this;
    }
  });
});
