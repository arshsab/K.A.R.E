(function() {
    var url = window.location.pathname;

    if (url === "/") {
        return;
    }

    var repo = url.substring(1);

    if (url[url.length - 1] === '/') {
        url = url.substring(0, url.length - 1);
    }

    $.get("/server/linear?repo=" + repo, function(data) {
        var i = 0;

        for (var repo in data) {
            repo = data[repo];
            var other = repo["RepoB"];

            i++;

            repo["descriptionB"] = repo["descriptionB"] || "";

            $(".results").append("" +
                "<article class=\"search-result row\" style=\"padding-bottom: 10px\">" +
                "<div class=\"col-xs-12 col-sm-12 col-md-3\"> <h1>" + i +"</h1> </div>" +
                "<div class=\"col-xs-12 col-sm-12 col-md-7\">" +
                "<h3><a href=\"https://github.com/" + other + "\">" + other + "</a></h3>" +
                "<p>" + repo["descriptionB"] + "</p>" +
                "<span class=\"plus\"><a href=\"../" +  other + "\"><i class=\"glyphicon glyphicon-plus\"></i></a></span>" +
                "</div>" +
                "</article>"
            );
        }

        $("#count").append(i + "");
    });

})();