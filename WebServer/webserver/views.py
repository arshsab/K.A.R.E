import bisect
from pyramid.view import view_config
from data import repos
from .data.models import Repo
from .data.recommenders import SimpleRecommender


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
    owner = request.matchdict['owner']
    repo = request.matchdict['repo']

    full_name = owner + '/' + repo

    recommender = SimpleRecommender()
    recommendations = recommender.recommendations_for(full_name)
    current_repo = Repo(full_name)

    return dict(recommendations=recommendations, current_repo=current_repo)


def _load_names(arr, dic):
    for repo in repos.find():
        for name in [repo['indexed_name'], repo['indexed_name'].split("/")[1]]:
            arr.append(name)

            if name not in dic or dic[name]['gazers'] < repo['gazers']:
                dic[name] = repo

    arr.sort()


names = []
details = {}
_load_names(names, details)

print('Loaded names for autocomplete function.')

@view_config(route_name='autocomplete', renderer='json')
def render_autocomplete(request):
    query = request.matchdict['query']

    if len(query) < 3:
        return {'results': []}

    i = bisect.bisect_left(names, query)

    if i < len(names) and names[i] < request:
        i += 1

    suggestions = []

    while i < len(names):
        fake = names[i]

        if not fake.startswith(query):
            break

        suggestions.append(names[i])
        i += 1

    suggestions.sort(key=lambda suggestion: details[suggestion]['gazers'], reverse=True)

    final = []
    sent = set()

    j = 0
    k = 0
    while j < 7 and k < len(suggestions):
        if details[suggestions[k]]['name'] not in sent:
            final.append(dict(name=details[suggestions[k]]['name']))
            j += 1

            sent.add(details[suggestions[k]]['name'])

        k += 1

    return {
        'results': final
    }