import bisect
from flask import g
import mongo


def _load_names(arr, dic, repos):
    for repo in repos.find():
        for name in [repo['indexed_name'], repo['indexed_name'].split("/")[1]]:
            arr.append(name)

            if name not in dic or dic[name]['gazers'] < repo['gazers']:
                dic[name] = repo

    arr.sort()


def autocomplete(query):
    names, details = _get_auto_vars()

    # Find repos who start with the query
    i = bisect.bisect_left(names, query)

    if i < len(names) and names[i] < query:
        i += 1

    suggestions = []

    while i < len(names):
        fake = names[i]

        if not fake.startswith(query):
            break

        suggestions.append(names[i])
        i += 1

    # Sort by importance
    suggestions.sort(key=lambda suggestion: details[suggestion]['gazers'], reverse=True)

    final = []
    sent = set()

    # Return the top 7.
    j = 0
    k = 0
    while j < 7 and k < len(suggestions):
        if details[suggestions[k]]['name'] not in sent:
            final.append(details[suggestions[k]]['name'])
            j += 1

            sent.add(details[suggestions[k]]['name'])

        k += 1

    return final


def _contains(a, x):
    i = bisect.bisect_left(a, x)
    return i != len(a) and a[i] == x

def fixup_query(query):
    names = _get_auto_vars()[0]

    if _contains(names, query) and "/" in query:
        return query

    suggestions = autocomplete(query)

    if len(suggestions) == 0:
        return None

    return suggestions[0]

def _get_auto_vars():
    # Check if it is already loaded.
    if not hasattr(g, 'auto'):
        names = []
        details = {}
        db = mongo.get_db()

        _load_names(names, details, db.repos)

        g.auto = (names, details)

    return g.auto