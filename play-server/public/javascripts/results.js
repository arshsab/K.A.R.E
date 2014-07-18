var url = window.location.href;
var searchedFor = url.substring(url.indexOf("search") + "search".length + 1);

var currentRepo = null;
var grade;

$(".result").click(function() {
    select($(this));
});

$(".back-button").click(mobileFlipSwitch);

function select(repo) {
    $(".result").each(function() {
        $(this).removeClass("result-selected");
    });

    var name = repo.attr('id');

    currentRepo = name;
    setGrade(5);
    $(".readme-feedback").removeClass("hide");
    $(".readme-thank-you").addClass("hide");

    $(document.getElementById(name)).addClass("result-selected");

    mobileFlipSwitch();
    loadReadMe(name);
}

var converter = new Showdown.converter();

function loadReadMe(repo) {
    var readme = $(".readme-inner");

    readme.css("opacity", "0");
    $.getJSON("https://api.github.com/repos/" + repo + "/readme", function(json) {
        readme.html(converter.makeHtml(decodeBase64Content(json.content)));
        readme.css("opacity", "1");
    });

    var readmeTitle = $(".readme-title");

    var nameOnly =  repo.substring(repo.indexOf("/") + 1) +
        "<a class='pull-right' href='https://github.com/" + repo + "'>" +
        "   <i class='fa fa-github-alt'></i>" +
        "</a>&nbsp&nbsp";

    readmeTitle.html(nameOnly);
}

function mobileFlipSwitch() {
    $(".kare-panel").toggleClass("hidden-xs");
    $(".back-button").toggleClass("hide");
}

$("#slider").slider({
    min: 1,
    max: 10,
    range: "min",
    value: 5,
    slide: function(_, ui) {
        setGrade(ui.value);
    }
});

function setGrade(newVal) {
    grade = newVal;
    $("#grade-display").html(grade);
}

$("#grade-submit").click(function() {
    var theGoods = {
        a: searchedFor,
        b: currentRepo,
        score: grade
    };

    $.ajax({
        type: 'POST',
        url: "/feedback",
        data: JSON.stringify(theGoods),
        contentType: "application/json; charset=utf-8",
        dataType: 'json'
    });

    $(".readme-feedback").addClass("hide");
    $(".readme-thank-you").removeClass("hide");
});

$(document).ready(function() {
    if (!isPhone())
        select($(".result").first())
});

function isPhone() {
    return window.innerWidth <= 768;
}
