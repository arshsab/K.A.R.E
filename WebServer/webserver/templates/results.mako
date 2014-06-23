<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="">
    <meta name="author" content="">

    <title>K.A.R.E</title>

    <link href="http://netdna.bootstrapcdn.com/font-awesome/4.1.0/css/font-awesome.min.css" rel="stylesheet">
    <link href='http://fonts.googleapis.com/css?family=Open+Sans:300,400,700' rel='stylesheet' type='text/css'>
    <link href="//maxcdn.bootstrapcdn.com/bootstrap/3.1.1/css/bootstrap.min.css" rel="stylesheet">
    <link href="/css/dashboard.css" rel="stylesheet">
    <link href="/css/typeaheadjs.css" rel="stylesheet">


</head>

<body>

<div class="navbar navbar-inverse navbar-fixed-top" role="navigation">
    <div class="container-fluid">
        <div class="navbar-header">
            <a class="navbar-brand" href="/">K.A.R.E</a>
        </div>
        <div>
            <ul class="nav navbar-nav navbar-right">

            </ul>
            <form id="results-form" class="navbar-form navbar-right">
                <input id="search-box" type="text" class="form-control" name="search" placeholder="${current_repo.name}">
                <input style="visibility: hidden; max-width: 0; width: 0" type="submit" />
            </form>
        </div>
    </div>
</div>

<div class="container-fluid">
    <div class="row">
        <div class="col-sm-4 col-md-3 sidebar">
            <ul class="nav nav-sidebar nav-list" id="results">
                % for recommendation in recommendations:
                    <li class="result" id="${recommendation.name}" style="opacity: 1;">
                        <div class="row">
                            <a class="col-xs-8 col-sm-8 col-md-8 readme-link" href="#" id="${recommendation.name.replace("/", "-")}" style="color: rgb(255, 255, 255);">${recommendation.name}</a>
                            <a class="col-xs-2 col-sm-2 col-md-2 icon-link" href="https://github.com/${recommendation.name}"><i class="fa fa-2x fa-github"></i></a>
                            <a class="col-xs-2 col-sm-2 col-md-2 icon-link" href="/search/${recommendation.name}"><i class="fa fa-2x fa-search"></i></a>
                        </div>
                        <div class="row suggestion-row">
                            <p class="description">${recommendation.description}</p>
                        </div>
                        <div class="row">
                            <div class="logistics">
                                <div class="col-xs-7 col-sm-7 col-md-7 left-log">
                                    <i class="fa fa-lg fa-angle-left"></i>&nbsp;<i class="fa fa-lg fa-angle-right"></i>&nbsp;${recommendation.language}
                                </div>
                                <div class="col-xs-5 col-sm-5 col-md-5 right-log">
                                    <i class="fa fa-lg fa-star"></i>&nbsp;${recommendation.stars}
                                </div>
                            </div>
                        </div>
                    </li>
                % endfor
            </ul>
        </div>
        <div class="col-sm-8 col-sm-offset-4 col-md-9 col-md-offset-3 main">
            <div id="readme">
            </div>
        </div>
    </div>
</div>

<!-- Bootstrap core JavaScript
================================================== -->
<!-- Placed at the end of the document so the pages load faster -->
<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.0/jquery.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/showdown/0.3.1/showdown.min.js"></script>
<script src="//maxcdn.bootstrapcdn.com/bootstrap/3.1.1/js/bootstrap.min.js"></script>
<script src="/js/results.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/typeahead.js/0.10.2/bloodhound.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/typeahead.js/0.10.2/typeahead.bundle.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/typeahead.js/0.10.2/typeahead.jquery.min.js"></script>
<script src="/js/search-bar.js"></script>

</body>
</html>

