var converter = new Showdown.converter();
var keys = [];

var addElem = function(key) {

    var id = key.split(".").join("").split('/').join('');
    var li = '<li style = "opacity: 0;" data-repo="' + id +'" id = "' + id + '" class = "result"><a href="#" class = "'+ id +'">' + key + '</a>'+
    '<a class = "dir-link" href="https:/github.com/' + key +
    '"><img class = "view-icon" src = "assets/github.png" align="bottom"></a></li>"';
    $("#results").append(li);

    keys.push(key);

    $("#" + id).on("click", function() {
        var text = $("." + id).text();
        console.log(text);
        $("#readme").fadeOut(50,
            function() {
                var content;
                $.get("https://api.github.com/repos/"+text +"/readme", 
                    function(e) {
                        console.log(e);
                        console.log(e.content);
                        content = atob(e.content); 
                        $("#readme").html(converter.makeHtml(content)).fadeIn(50);
                    });
            });
    });

    $("#" +  id).animate( {"opacity": "1"}, 50);
};

var popRes = function(resKeys) {
    for (var i = 0; i < resKeys.length; i++) {
        var id = resKeys[i].split("/").join("");
        console.log(resKeys[i]);
        addElem(resKeys[i]);
    }
};

var getResult = function() {
    window.location.href = "results.html#" + $("#search").val();
};

$("#search").keyup(function(event){
    if(event.keyCode == 13){
        $("#button").click();
    }
});

$("#search-box").keyup(function(event){
    if(event.keyCode == 13){
        window.location.href = "results.html#" + $("#search-box").val();
    }
});

var getParams = function() {
    return document.location.search.replace(/(^\?)/,'').split('&').reduce(function(o,n){n=n.split('=');o[n[0]]=n[1];return o},{});
};


$(document).ready(function() {
    var query = decodeURIComponent(getParams()["search"]);
        addElem(query);
});