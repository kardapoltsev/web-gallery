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
      var tags = this.$("#input-tags").tagsinput('items');
      this.model.save({tags: tags}, {patch: true});
    },


    render: function() {
      this.$el.html(this.template(this.model.toJSON()));

      $("#input-tags").tagsinput('input');

      $("#input-tags").tagsinput("input").typeahead({
        hint: true,
        highlight: true,
        minLength: 1
      }, {
        name: "tags",
        displayKey: "name",
        source: function(q, cb){
          var url = "/api/search/tags?term=" + q;
          $.get(url).done(function(data){
            cb(data.tags);
          }).fail(function(){
            cb([])
          });
      }}).bind('typeahead:selected', $.proxy(function (e, item) {
        console.log("selected" + item);
        this.tagsinput('add', item.name);
        this.tagsinput('input').typeahead('val', '');
      }, $("#input-tags")));

      return this;
    }
  })
});
