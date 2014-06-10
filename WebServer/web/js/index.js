var converter = new Showdown.converter();

var addElem = function (data) {
    var id = data.name.split(".").join("").split('/').join('');
    console.log("id: " + id);
    var li = '<li class="result" style="opacity: 0;"' +  '" id="' + id + '">' +
                '<a href="#" class="reslink ' + id + '">' + data.name + '</a>' +
                '<a class = "dir-link" href="https:/github.com/' + data.name +
        '"><p>' + data.description +  '</p><p><b>lang:</b>' + data.language + '</p><p><b>stars:</b>' + data.stars +  '</p><img class="view-icon" src="assets/github.png" align="bottom"></a></li>"';
    $("#results").append(li);



//    $.getJSON("https://api.github.com/repos/" + key + "/readme", function (e) {
//            var content = atob(e.content);
//            $("#readme").html(converter.makeHtml(content));
//   });
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
        console.log(data);
        console.log(query);
        for (var i = 0; i < data.length; i++) {
            addElem(data[i]);
        }
    });
});
