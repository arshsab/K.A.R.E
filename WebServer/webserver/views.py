from pyramid.view import view_config


@view_config(route_name='home', renderer='templates/index.mako')
@view_config(route_name='index', renderer='templates/index.mako')
def render_index(request):
    return {}


@view_config(route_name='about', renderer='templates/about.mako')
def render_about(request):
    return {}


@view_config(route_name='404', renderer='templates/404.mako')
def render_404(request):
    return {}


@view_config(route_name='results', renderer='templates/results.mako')
def render_results(request):
    pass