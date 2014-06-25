import bisect
import urllib.parse
from pyramid.view import view_config
from data import repos, meta, stars, scores, db
from .data.models import Repo
from .data.recommenders import SimpleRecommender

def _load_names(arr, dic):
    for repo in repos.find():
        for name in [repo['indexed_name'], repo['indexed_name'].split("/")[1]]:
            arr.append(name)

            if name not in dic or dic[name]['gazers'] < repo['gazers']:
                dic[name] = repo

    arr.sort()


_names = []
_details = {}
_load_names(_names, _details)

print('Loaded names for autocomplete function.')

@view_config(route_name='home', renderer='templates/index.mako')
@view_config(route_name='index', renderer='templates/index.mako')
def render_index(request):
    return {}


@view_config(route_name='about', renderer='templates/about.mako')
def render_about(request):
    repo_count = repos.count()

    return dict(repo_count=repo_count)


@view_config(route_name='statistics', renderer='templates/statistics.mako')
def render_statistics(request):
    repos_count = repos.count()
    stars_count = stars.count()
    scores_count = scores.count()
    gigabytes = db.command('dbstats')['fileSize'] / (1024 ** 3)

    current_task = meta.find_one({'role': 'current_task'})['value']
    redos = meta.find_one({'role': 'redos'})['value']
    stars_done = meta.find_one({'role': 'stars_done'})['value']
    correlations_done = meta.find_one({'role': 'correlations_done'})['value']
    crashes = meta.find_one({'role': 'crashes'})['value']

    return dict(
        current_task=current_task,
        redos=redos,
        stars_done=stars_done,
        correlations_done=correlations_done,
        crashes=crashes,
        repos_count=repos_count,
        stars_count=stars_count,
        scores_count=scores_count,
        gigabytes=gigabytes
    )


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


@view_config(route_name='autocomplete', renderer='json')
def render_autocomplete(request):
    query = urllib.parse.unquote(request.matchdict['query'])

    if len(query) < 3:
        return {'results': []}

    i = bisect.bisect_left(_names, query)

    if i < len(_names) and _names[i] < query:
        i += 1

    suggestions = []

    while i < len(_names):
        fake = _names[i]

        if not fake.startswith(query):
            break

        suggestions.append(_names[i])
        i += 1

    suggestions.sort(key=lambda suggestion: _details[suggestion]['gazers'], reverse=True)

    final = []
    sent = set()

    j = 0
    k = 0
    while j < 7 and k < len(suggestions):
        if _details[suggestions[k]]['name'] not in sent:
            final.append(dict(name=_details[suggestions[k]]['name']))
            j += 1

            sent.add(_details[suggestions[k]]['name'])

        k += 1

    return {
        'results': final
    }