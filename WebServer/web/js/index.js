var converter = new Showdown.converter();
var results = new Array();

$(".page-header").text("");

var keys = new Array();

var addElem = function(key) {
	var li = '<li style = "opacity: 0;" id = "' + key[0]+key[1] + '" class = "result"><a href="#" class = "'+ key[0]+key[1] +'">' + key[0] + "/" + key[1] + '</a>'+
	'<a class = "dir-link" href="https:/github.com/' + key[0] + "/" + key[1]+
	'"><img class = "view-icon" src = "assets/github.png"></a></li>"';
	$("#results").append(li);
	keys.push(key);
	$("#" + key[0]+key[1]).on("click", function() {
		var text = $("." + key[0]+key[1]).text();
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
};

var popRes = function(resKeys) {
	for (var i = 0; i < resKeys.length; i++) {
		addElem(resKeys[i]);
		$("#" + resKeys[i][0] + resKeys[i][1]).animate( {"opacity": "1"}, 50);
	}
};
