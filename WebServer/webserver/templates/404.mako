<!DOCTYPE html>
<html>
<head lang="en">
    <meta charset="UTF-8">
    <title>404</title>
    <link href='http://fonts.googleapis.com/css?family=Dosis:300,400,700' rel='stylesheet' type='text/css'>
    <link href="http://netdna.bootstrapcdn.com/font-awesome/4.1.0/css/font-awesome.min.css" rel="stylesheet">
    <style type="text/css">
    	body {
    		font-family: 'Dosis';
    		background-color: #000000;
    		color: #FFFFFF;
    		max-height: 90%;
    		
    	}

    	h1 {
    		width: 100%;
    		font-weight: 400;
    		text-align: center;
    		padding-top: 2%;
    		font-size: 400%;
    	}

    	p {
    		font-size: 250%;
    		font-weight: 400;
    		padding: 20%;
    		padding-top: 5%;
    		padding-bottom: 0px;
    	}

    	a {
    		text-decoration: none;
    		float: right;
    		font-weight: 700;
    		transition: all 200ms linear;
    		-webkit-transition: all 200ms linear;
    	}
    	a:visited {
    		color: #FFFFFF;
    		transition: all 200ms linear;
    		-webkit-transition: all 200ms linear;	
    	}
    	a:hover {
    		color: #6ED3FF;
    		transition: all 200ms linear;
    		-webkit-transition: all 200ms linear;	
    	}
    	#back {
    		position: fixed;
			left: 0;
			right: 0;
			z-index: -1;
			width: 100%;
			height: 100%;
			display: block;	
			background-image: url("assets/base.png");
			background-repeat: no-repeat;
			background-size: 100%;
    		filter: blur(5px) brightness(0.75);
		    -webkit-filter: blur(5px) brightness(0.75);
		    -moz-filter: blur(5px) brightness(0.75);
		    -ms-filter: blur(5px) brightness(0.75);
		    -o-filter: blur(5px) brightness(0.75);
    	}

    	#text {
    		z-index: 9999 !important;
    	}

    	.fa {
    		font-size: 350%;
    		text-align: center;
    		padding-left: 45%;
    	}

    </style>
</head>
<body>
	<div id = "back"></div>
    <div id = "text"><h1>ALL YOUR REPOS ARE <i><b>NOT</b></i> &nbsp; BELONG TO US</h1>
    <p>The repo that you tried to find is either not valid, has too few stars for us to make useful recommendations, or we have not scraped it yet.
    <br><br><i class="fa fa-frown-o"></i><br><br><a href="/">Click here to return to the home page.</a></p>
</body>
</html>