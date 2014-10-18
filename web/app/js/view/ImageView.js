/**
 * Created by alexey on 6/4/14.
 */
define(function(require){

  var $ = require("jquery"),
      Backbone = require("backbone"),
      Tag = require("model/Tag"),
      CommentView = require("view/CommentView"),
      CommentList = require("collection/CommentList"),
      Comment = require("model/Comment")
      ;


  return Backbone.View.extend({
    rootUrl: "/images",
    id: "image-details",
    tagName: "div",
    className: "row",
    template: _.template($('#image-details-tpl').html()),

    events: {

    },


    initialize: function () {
      $("#main").html(this.el);
      this.render();
      this.listenTo(this.model, 'sync', this.render);
    },


    onAddCommentClick: function () {
      var textarea = $("#comment-text");
      var text = textarea.val();
      textarea.val("");
      this.createComment(text, null);
    },


    setLikeButtonText: function(){
      var text = this.model.get("isLiked") ? "Unlike" : "Like";
      $("#like").html(text);
    },


    onLikeClick: function(){
      var likeButton = $("#like");
      likeButton.attr("disabled", "disabled");
      var isLiked = this.model.get("isLiked");
      var method = isLiked ? "DELETE" : "POST";
      $.ajax({
        method: method,
        url: "/api/images/" + this.model.id + "/likes",
        context: this,
        success: function(){
          this.model.set("isLiked", !isLiked);
          var inc = isLiked ? -1 : 1;
          this.model.set("likesCount", this.model.get("likesCount") + inc);
          $("#likes-count").html(this.model.get("likesCount"));
          this.setLikeButtonText();
        },
        complete: function(){
          likeButton.removeAttr("disabled");
          console.log("like " + method + " success")
        }.bind(this),
        error: function(){
          console.log("like " + method + " failed")
        }
      });
    },


    createComment: function(text, parentId) {
      console.log("new comment text: " + text + "parent: " + parentId);
      var comment = new Comment({
        imageId: this.model.id,
        text: text,
        parentCommentId: parentId,
        owner: window.galleryUser.toJSON()
      });
      comment.save({}, {
        success: function(c){this.comments.add([c])}.bind(this)
      });

    },


    addTag: function(t) {
      console.log("addTag");
      var input = $("#input-tags");

      var tags = input.tagsinput("items");
      var existing = _.findWhere(tags, {name: t.name});
      if(existing){
        console.log("already exists, skipping");
        t.id = existing.id
      } else {
        var tag;
        if(t.id == -1){
          console.log("creating new tag");
          console.log(t);
          tag = new Tag();
          tag.save({name: t.name, ownerId: window.galleryUser.id}, {async: false});
          $(document).trigger("tagAdded");
          t.id = tag.id
        } else {
          console.log("adding existing tag");
          tag = new Tag(t);
        }
        this.model.set("tags", tag.toJSON(), {remove: false});
        this.model.save(null, {patch: true});
      }

      console.log("adding to tagsinput");
      console.log(t);
      input.tagsinput('add', t);
      input.tagsinput('input').typeahead('val', '');
    },


    render: function() {
      console.log("render ImageView");
      this.$el.html(this.template(this.model.toJSON()));
      this.initTagsInput();
      this.initPopup();
      this.initComments();
      this.setLikeButtonText();

      $("#add-comment").click(this.onAddCommentClick.bind(this));
      $("#like").click(this.onLikeClick.bind(this));

      return this;
    },


    initComments: function(){
      console.log("init comments");
      this.comments = new CommentList([],{imageId: this.model.id});
      this.listenTo(this.comments, 'add', this.addComment);
      this.comments.fetch();
    },


    addComment: function(comment){
      var commentView = new CommentView({model: comment});
      this.listenTo(commentView, "reply-added", this.addReply)
    },


    addReply: function(parent, reply) {
      console.log(parent, reply);
      this.createComment(reply.text, parent.id)
    },


    onEnterPressed: function(e){
      var input = $("#input-tags");
      var tag = input.tagsinput("input").val();
      if(e.which == 13) {
        var item = {id: -1, name: tag};
        this.addTag(item);
      }
    },


    initTagsInput: function () {
      $("#input-tags").tagsinput({
        itemValue: "id",
        itemText: "name",
        trimValue: true
      });
      this.model.get("tags").each(function(t){
        $("#input-tags").tagsinput('add', t.toJSON());
      });

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
            //allow new tag creation
            if(data.tags.length == 0){
              var newTag = {id: -1, name: q};
              data.tags.push(newTag);
            }
            cb(data.tags);
          }).fail(function(){
            cb([])
          });
        }})
      $("#input-tags").tagsinput("input").on('typeahead:selected', function (e, item) {
        console.log("selected" + item);
        this.addTag(item)
      }.bind(this));

      $("#input-tags").tagsinput("input").keyup(this.onEnterPressed.bind(this));

    },


    initPopup: function(){
      this.$el.magnificPopup({
        type: "image",
        delegate: '.image-link',
        tLoading: "Loading image #%curr%...",
        mainClass: "mfp-with-zoom",
        gallery: {
          enabled: true,
          navigateByImgClick: true,
          preload: [0,1]
        },
        image: {
          tError: '<a href="%url%">The image #%curr%</a> could not be loaded.'
        }
      });
    }
  })
});
