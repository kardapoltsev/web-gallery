/*jslint unparam: true */
/*global window, document, blueimp, $ */

$(function () {
    'use strict';

//    function loadImages(url){
//        $.ajax({
//            type: "POST",
//            url: url,
//            jsonp: 'jsoncallback'
//        }).done(function (result) {
//            var linksContainer = $('#links');
//            linksContainer.empty();
//            console.log(result);
//
//            $.each(result, function (index, img) {
//                console.log(img);
//                $('<a/>')
//                    .append($('<img>').prop('src', "/thumbnails/" + img.filename))
//                    .prop('href', "/images/" + img.filename)
//                    .attr('data-gallery', '')
//                    .appendTo(linksContainer);
//            });
//        });
//    }

    $('#borderless-checkbox').on('change', function () {
        var borderless = $(this).is(':checked');
        $('#blueimp-gallery').data('useBootstrapModal', !borderless);
        $('#blueimp-gallery').toggleClass('blueimp-gallery-controls', borderless);
    });

    $('#fullscreen-checkbox').on('change', function () {
        $('#blueimp-gallery').data('fullScreen', $(this).is(':checked'));
    });


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

