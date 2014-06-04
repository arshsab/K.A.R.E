var setVis = function (opacity) {
	$(".page-header").css("opacity", opacity);
}


$(".result").on("click", function() {
	setVis("1");
	var text = $(this).text();
	$(".page-header").fadeOut(200,
		function() {
			$(".page-header").text(text).fadeIn(200);
		});
});
