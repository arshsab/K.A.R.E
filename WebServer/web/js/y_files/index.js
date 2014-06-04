var setVis = function (opacity) {
	$(".page-header").css("opacity", opacity);
}


$(".result").on("click", function() {
	$(".page-header").fadeOut(300, 
		function() { 
			$(".page-header").text( $(this).text()).fadeIn(300); 
		});
});
