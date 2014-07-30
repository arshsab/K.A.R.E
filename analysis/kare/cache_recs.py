import pymongo
from recommenders import SVRRecommender


def id_to_name(recommendations, repos):
    """
    :param recommendations: list in form [(score, r_id)]
    :param repos: ref to the collection in mongo
    :return: [(score, 'user/repo')]
    """
    ret = []
    for rec in recommendations:
        ret.append((rec[0], repos.find_one({'r_id': rec[1]})['indexed_name']))
    return ret


def main():
    """
    should go through every repo in the repos collection
    and then get the recommendations for that, and lookup the names based on
    the r_id and then store the recs in the cached_recs collection with the format
    {
        repo: 'git/git',
        recs: [
            (score, 'user/repo'),
            ...
        ]
    }
    note that the python 'tuple' gets converted to a regular list in mongo
    and that the non-unicode strings that are default get converted to unicode
    strings in mongo (i.e. u'foobar')
    """
    db = pymongo.MongoClient().kare
    rec = SVRRecommender(db)
    cached_recs = db.cached_recs
    for i, repo in enumerate(db.repos.find()):
        recommendations = id_to_name(rec.get_recommendations(repo['r_id']), db.repos)
        cached_recs.insert({'repo': repo['indexed_name'], 'recs': recommendations})

        print("Done with: %s [%d]" % (repo['name'], i))


if __name__ == '__main__':
    main()