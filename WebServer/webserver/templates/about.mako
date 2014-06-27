<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>About the team</title>

    <!-- Bootstrap -->
    <link href="//maxcdn.bootstrapcdn.com/bootstrap/3.1.1/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="css/about.css"/>
    <link href="http://netdna.bootstrapcdn.com/font-awesome/4.1.0/css/font-awesome.min.css" rel="stylesheet">


    <!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>
    <script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
    <script src="https://oss.maxcdn.com/libs/respond.js/1.4.2/respond.min.js"></script>
    <![endif]-->
</head>
<body>


<div class="container">
    <div class="row">
        <div class="col-md-12 text-center">
            <h1 style="font-size: 54px; font-weight: normal; padding-top: 10%;"h><a href="/">K.A.R.E</a></h1>
        </div>
    </div>
    <div class="row">
        <div class="col-md-12">
            <h1>About the project</h1>
        </div>
    </div>
    <div class="row">
        <div class="col-md-12" style="font-size: 18px">
            <p>K.A.R.E stands for the Kick-Ass Recommendation Engine for Github. The project takes advantage of Githubs starring feature and API in order to understand relationships between repositories and recommend repositories to users. Unfortunately, because of Github's rate limiting we were unable to get all of Github's repos. Instead our program has scraped the top ${str(repo_count)} repos. If a repo is not on here it is likely because it has not been scraped yet or it does not meet the minimum threshold for being scraped. </p>
            <p>Some statistics about the project are maintained on <a href="statistics">the status page</a>.</p>
        </div>
    </div>
    <div class="row">
        <div class="col-md-12">
            <h1>About the team</h1>
        </div>
    </div>
    <div class="row">
        <div class="col-md-4">
            <h1>Arshdeep:</h1>
            <p><i class="fa fa-github"></i> <a href="https://github.com/arshsab">arshsab</a></p>
            <p><i class="fa fa-linkedin-square"></i> <a href="https://www.linkedin.com/in/arshsab">arshsab</a></p>
            <p>Backend, Data</p>
        </div>
        <div class="col-md-4">
            <h1>Adrian:</h1>
            <p><i class="fa fa-github"></i> <a href="https://github.com/adrianc-a">adrianc-a</a></p>
            <p><i class="fa fa-linkedin-square"></i> <a href="https://www.linkedin.com/in/adrianca">adrianca</a></p>
            <p><a href="https://adrianc-a.github.io">adrianc-a.github.io</a></p>
            <p>Backend, Frontend</p>
        </div>
        <div class="col-md-4">
            <h1>Ritwik:</h1>
            <p><i class="fa fa-github"></i> <a href="https://github.com/ritwikd">ritwikd</a></p>
            <p><i class="fa fa-linkedin-square"></i> <a href="https://www.linkedin.com/in/ritwikduttausa">ritwikduttausa</a></p>
            <p><a href="http://ritwikd.com">ritwikd.com</a></p>
            <p>Frontend</p>
        </div>
    </div>
</div>
<!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.0/jquery.min.js"></script>
<!-- Include all compiled plugins (below), or include individual files as needed -->
<script src="//maxcdn.bootstrapcdn.com/bootstrap/3.1.1/js/bootstrap.min.js"></script>
</body>
</html>