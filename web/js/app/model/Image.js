/**
 * Created by alexey on 6/3/14.
 */
define(function(require){

  var Backbone = require("backbone")
      ;

  return Backbone.Model.extend({
    defaults: function(){
      return {
        id: null,
        filename: null,
        tags: []
      }
    },


    addTag: function(tag){
      console.log(this);
      var tags = this.get("tags").push(tag);
//      this.set("tags", this.tags.push(tag);
      console.log(this.get("tags"))
    }
  });
});
