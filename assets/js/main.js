/*jslint unparam: true */
/*global window, document, blueimp, $ */

$(function () {
    'use strict';

  $('#blueimp-gallery').data('useBootstrapModal', false);
  $('#blueimp-gallery').toggleClass('blueimp-gallery-controls', true);

  $("#input-search-tags").autocomplete({
    serviceUrl: '/search/tags',
    onSelect: function (suggestion) {
      window.location = "/tags/" + suggestion.value
    }
  });

  $('.btn-add-tag').click(function(item) {
    var id = item.target.getAttribute("value");
    var input = $("#input-add-tag-" + id);
    var tag = input.val();
    input.val("");
    console.log("add tag " + tag + " to " + id);
    $.ajax({
      type: "PATCH",
      url: "/images/" + id,
      data: {tag: tag},
      jsonp: false
    }).done(function (result) {
      $("#image-tags-" + id).append(", " + tag);
    });
  });
});

