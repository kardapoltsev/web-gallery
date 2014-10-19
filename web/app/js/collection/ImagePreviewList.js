/**
 * Created by alexey on 6/3/14.
 */
define(function(require){

  var $ = require("jquery"),
      Backbone = require("backbone"),
      Image = require("model/Image")
      ;

  return Backbone.Collection.extend({
    model: Image,
    baseUrl: "",
    query: "",
    limit: 20,
    offset: 0,
    moreAvailable: true,
    url: function(){

      var url = this.baseUrl + "?offset=" + this.offset + "&limit=" + this.limit + "&" + this.query;
      console.log("url: " + url)
      return this.baseUrl + "?offset=" + this.offset + "&limit=" + this.limit + "&" + this.query;
    },
    parse: function (response) {
      this.moreAvailable = response.images.length > 0;
      this.offset += response.images.length;
      return response.images
    },
    loadMore: function() {
      if(this.moreAvailable){
        console.log("loading more items... offset = " + this.offset);
        this.fetch({remove: false, async: false});
      }
    }
  });
});
