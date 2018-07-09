'use strict';

$('.ui.accordion').accordion({
    selector: {}
});

$(document).ready(function () {

    // colorize();

    $('.special.cards .image').dimmer({
        on: 'hover'
    });

    $(".ui.rating").rating(); //rating trigger

    $('.tabular .item').tab();

    $(".hamburger").on("click", function () {
        this
            .classList
            .toggle("is-active");
    });

    $('.ui.embed').embed();

});

$(".modalshow").on("click", function () {
    $(".ui.modal").modal({
        inverted: false,
        blurring: true
    }).modal("show");
});

$(".addbtn").on("click", function () {
    $('#example1')
        .progress('increment');
});

$(".unaddbtn").on("click", function () {
    $('#example1')
        .progress('decrement');
});

$('.menu .item').tab();

$('.ui.button').popup({
    inline: true
});

$('.clickpopup').popup({
    on: 'click'
});

$(".ani.button").on("click", function () {
    var anitype = $(this).attr("data-ani");
    $('.ani.image').transition(anitype);
});

