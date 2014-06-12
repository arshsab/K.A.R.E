var converter = new Showdown.converter();
var readmeArr = [];

var addElem = function (data) {
    var li = '<li class="result"' +  '" id="' + data.name + '">' +
        '<a class = "gitlink" href="https://github.com/' + data.name + 
        '""><img class="gitim" src="assets/github.png"></a>' +  
        '<a href="#" class="reslink">' + data.name + '</a>' +
        '<div id = "info"><div class  = "dlink">' + data.description +  
        '</div><br><br><div class  = "dlink">' + 
        '<i class="fa fa-code"></i><b>' + data.language +
        '</b></div><div class  = "dlink">' +
        '<i class="fa fa-star"></i><b>' + data.stars +  
        '</b></div>'+
        '</div></li>"';
    $("#results").append(li);
    $(".result").css("opacity", "1");
    $("#" + data.name).on("click", function(e) {
        $.getJSON("https://api.github.com/repos/" + data.name + "/readme", function(json) {
            $("#" + data.name).html(converter.makeHtml(atob(json.content)));
        });
    });
};

var getParams = function () {
    return document.location.search.replace(/(^\?)/, '').split('&').reduce(function (o, n) {
        n = n.split('=');
        o[n[0]] = n[1];
        return o
    }, {});
};

$(document).ready(function () {
    var query = decodeURIComponent(getParams()["search"]);
    var arr = query.split("/");
    var curQuery = "";
    $.getJSON("/searchjson?owner=" + arr[0] + "&repo="  + arr[1], function (data) {
        for (var i = 0; i < data.length; i++) {
            addElem(data[i]);
        };
    });
});
