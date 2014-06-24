<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Status Page</title>

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
            <h1>Database</h1>
        </div>
    </div>
    <div class="row">
        <div class="col-md-12" style="font-size: 18px">
            <p>K.A.R.E's data is stored in a database. Here are some statistics about this database.</p>
        </div>
    </div>
    <div class="col-md-12" style="font-size: 18px;">
        <div class="row">
            <ul>
                <li>Repos Found: <span style="font-family: monospace">${repos_count}</span></li>
                <li>Stars Scraped: <span style="font-family: monospace">${stars_count}</span></li>
                <li>Relationships Built: <span style="font-family: monospace">${scores_count}</span></li>
                <li>Total size in GB: <span style="font-family: monospace">${gigabytes}</span></li>
            </ul>
        </div>
    </div>
    <div class="row">
        <div class="col-md-12">
            <h1>Update Cycle</h1>
        </div>
    </div>
    <div class="row">
        <div class="col-md-12" style="font-size: 18px">
            <p>K.A.R.E constantly runs update cycles to ensure that its database is up to speed with the latest repositories. Below are statistics on the current update cycle.</p>
        </div>
    </div>
    <div class="statistics col-md-12" style="font-size: 18px; font-family: monospace">
        <div class="row reports">
            The program has had: <b>${crashes}</b> errors in this cycle.<br>
            % for i in [1]:
                % if current_task == 'setup':
                    Setting up for an update cycle.<br>
                    <% break %>
                % else:
                    Update Cycle ready.<br>
                % endif

                % if current_task == 'repo_updates':
                    In progress: Finding new and out of date repos.<br>
                    <% break %>
                % else:
                    Finished finding a total of <b>${redos}</b> repos to update.<br>
                % endif

                % if current_task == 'star_updates':
                    In Progress: Finding <b>${stars_done}</b> / <b>${redos}</b> new and out of date repos.<br>
                    <% break %>
                % else:
                    Finished updating all of the stars.<br>
                % endif

                % if current_task == 'correlation_updates':
                    In Progress: Building correlations for <b>${correlations_done}</b> / <b>${redo}</b> new and out of date repos.<br>
                    <% break %>
                % else:
                    Finished updating all of the stars.<br>
                % endif

                % if current_task == 'cleanup':
                    Finished the update cycle.<br>
                    <% break %>
                % endif
            % endfor
        </div>
    </div>
</div>
<!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.0/jquery.min.js"></script>
<!-- Include all compiled plugins (below), or include individual files as needed -->
<script src="//maxcdn.bootstrapcdn.com/bootstrap/3.1.1/js/bootstrap.min.js"></script>
</body>
</html>