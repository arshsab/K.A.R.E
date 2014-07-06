from collections import namedtuple
import pymongo
import mongo
from flask import g


def recommend(repo_name):
    db = mongo.get_db()

    from_name, from_rid, position_mappings = _get_reco_vars()

    repo = from_name[repo_name]
    r_id = repo['r_id']

    size = len(position_mappings)

    results = []

    pos = 0
    for other in db.scores.find({'a': r_id}).sort([('s', pymongo.DESCENDING)]):
        if other['a'] == other['b']:
            continue

        expected = position_mappings[other['b']]
        actual = pos

        numer = abs(actual - expected)
        denom = (size - expected - 1) if actual > expected else expected

        percentage = numer / float(denom)
        results.append((other['b'], percentage))

        pos += 1

    results.sort(key=lambda tup: tup[1], reverse=True)

    final = [from_rid[rid] for rid, percentage in results]

    return final


def _get_reco_vars():
    if not hasattr(g, 'reco'):
        db = mongo.get_db()

        from_name = {}
        from_rid = {}
        position_mappings = {}

        i = 0
        for repo in db.repos.find().sort([('gazers', pymongo.DESCENDING)]):
            from_name[repo['indexed_name']] = repo
            from_rid[repo['r_id']] = repo
            position_mappings[repo['r_id']] = i

            i += 1

        Auto = namedtuple('Auto', ['from_name', 'from_rid', 'position_mappings'])

        g.auto = Auto(from_name=from_name, from_rid=from_rid, position_mappings=position_mappings)

    return g.auto