from pymongo import MongoClient
from pyramid.view import view_config


def setup_mongo():
    client = MongoClient()
    db = client.kare

    return db.scores, db.stars, db.repos

scores, stars, repos = setup_mongo()

@view_config(route_name='home', renderer='templates/index.mako')
@view_config(route_name='index', renderer='templates/index.mako')
def render_index(request):
    return {}


@view_config(route_name='about', renderer='templates/about.mako')
def render_about(request):
    repo_count = repos.count()

    return dict(repo_count=repo_count)


@view_config(route_name='404', renderer='templates/404.mako')
def render_404(request):
    return {}


@view_config(route_name='results', renderer='templates/results.mako')
def render_results(request):
    return {}