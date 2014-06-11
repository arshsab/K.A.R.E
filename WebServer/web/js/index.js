var converter = new Showdown.converter();

var addElem = function (data) {
    var li = '<li class="result"' +  '" id="' + data.name + '">' +
                '<a href="#" class="reslink ' + data.name + '">' + data.name + '</a>' +
                '<a class = "dir-link" href="https:/github.com/' + data.name +
        '"><p class  = "dlink">' + data.description +  '</p><p class  = "dlink">' + 
        '<b>Languages</b>' +  data.language + 
        '</p><p class  = "dlink"><b>Stars</b>' + data.stars +  
        '</p><img class="view-icon" src="assets/github.png" align="bottom"></a></li>"';
    $("#results").append(li);
    $(".result").css("opacity", "1");



   
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
    var arr = query.split("/");
    $.getJSON("/searchjson?owner=" + arr[0] + "&repo="  + arr[1], function (data) {
        for (var i = 0; i < data.length; i++) {
            addElem(data[i]);
        }
    });
});
