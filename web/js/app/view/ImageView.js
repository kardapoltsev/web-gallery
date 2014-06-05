/**
 * Created by alexey on 6/4/14.
 */
define(function(require){

  var $ = require("jquery"),
      Backbone = require("backbone"),
      Tag = require("app/model/Tag")
      ;


  return Backbone.View.extend({
    rootUrl: "/images",
    id: "image-details",
    tagName: "div",
    className: "row",
    template: _.template($('#image-details-tpl').html()),

    events: {
      "change input": "addTag"
    },

    initialize: function () {
      $("#main").html(this.el);
      this.render();
      this.listenTo(this.model, 'sync', this.render);
    },


    addTag: function() {
      console.log("adding tags")
      var tagNames = this.$("#input-tags").tagsinput('items');
      var tags = _.map(tagNames, function(name){
        var t = Tag.findOrCreate({name: name});
        t.fetch({async: false});
        return t;
      });

      console.log(tags);

      this.model.save({tags: tags}, {patch: true});
    },


    render: function() {
      console.log("render image view")
      this.$el.html(this.template(this.model.toJSON()));
      this.$("input").tagsinput("refresh");
      return this;
    }
  })
});
