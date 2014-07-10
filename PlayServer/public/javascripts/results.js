$(".result").click(function() {
    select($(this));
});

$(".back-button").click(mobileFlipSwitch);

function select(repo) {
    $(".result").each(function() {
        $(this).removeClass("result-selected");
    });

    var name = repo.attr('id');
    $(document.getElementById(name)).addClass("result-selected");

    mobileFlipSwitch();
    loadReadMe(name);
}

var converter = new Showdown.converter();

function loadReadMe(repo) {
    var readme = $(".readme-inner");

    readme.css("opacity", "0");
    $.getJSON("https://api.github.com/repos/" + repo + "/readme", function(json) {
        readme.html(converter.makeHtml(UTF8ArrToStr(base64DecToArr(json.content))));
        readme.css("opacity", "1");
    });

    var readmeTitle = $(".readme-title");

    readmeTitle.click(function() {
        window.location.href = "https://github.com/" + repo;
    });

    var nameOnly =  repo.substring(repo.indexOf("/") + 1) +
        "<a class='pull-right' href='" + "https://github.com/" + repo + "'>" +
        "   <i class='fa fa-github-alt'></i>" +
        "</a>&nbsp&nbsp";

    readmeTitle.html(nameOnly);
}

function mobileFlipSwitch() {
    $(".kare-panel").toggleClass("hidden-xs");
    $(".back-button").toggleClass("hide");
}

$(document).ready(function() {
    if (!isPhone())
        select($(".result").first())
});

function isPhone() {
    return window.innerWidth <= 768;
}
