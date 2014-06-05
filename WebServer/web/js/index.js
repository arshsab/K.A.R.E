var converter = new Showdown.converter();
var keys = new Array();

var addElem = function(key) {

	var id = key.split("/").join("");
	var li = '<li style = "opacity: 0;" id = "' + id + '" class = "result"><a href="#" class = "'+ id +'">' + key + '</a>'+
	'<a class = "dir-link" href="https:/github.com/' + key +
	'"><img class = "view-icon" src = "assets/github.png"></a></li>"';
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
						content = atob(e.content); 
						$("#readme").html(converter.makeHtml(content)).fadeIn(50);
					});
			});
	});

	$("#" + id).animate( {"opacity": "1"}, 50);
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
    	alert($("#search-box").val());
        window.location.href = "results.html#" + $("#search-box").val();
    }
});

$(document).ready(function() {
	if (window.location.hash.split("/").length > 1) {
		var repo = window.location.hash.substring(1);
		addElem(repo);
	}
});