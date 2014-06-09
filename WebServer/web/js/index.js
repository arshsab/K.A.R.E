var converter = new Showdown.converter();
var keys = [];

var addElem = function (key, data) {
    var id = key.split(".").join("").split('/').join('');
    var li = '<li class="result" style="opacity: 0;" data-repo="' + id + '" id="' + id + '"><a href="#" class="reslink ' + id + '">' + key + '</a>' +
        '<a class = "dir-link" href="https:/github.com/' + key +
        '"><p>' + data.description +  '</p><p><b>lang:</b>' + data.language + '</p><p><b>stars:</b>' + data.stars +  '</p><img class="view-icon" src="assets/github.png" align="bottom"></a></li>"';
    $("#results").append(li);

    keys.push(key);

    var text = $("." + id).text();
    $.getJSON("https://api.github.com/repos/" + text + "/readme",
        function (e) {
            content = atob(e.content);
            $(".col-md-2").css("width", "22%");
            $("#readme").html(converter.makeHtml(content));
        });

    $("#" + id).animate({"opacity": "1"}, 150);
};

var getParams = function () {
    return document.location.search.replace(/(^\?)/, '').split('&').reduce(function (o, n) {
        n = n.split('=');
        o[n[0]] = n[1];
        return o
    }, {});
};


$(".col-md-2").hover(function () {
    $(".col-md-2").css("width", "28%");
    $("#readme").css("padding-left", "14%");
}, function () {
    $(".col-md-2").css("width", "22%");
    $("#readme").css("padding-left", "6%");
});


$(document).ready(function () {
    var query = decodeURIComponent(getParams()["search"]);
    $.getJSON("/search?repo=" + query, function (data) {
        addElem(query, data);
    });
});