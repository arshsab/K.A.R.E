from __future__ import division

import numpy as np
from pymongo import MongoClient
import pymongo
from sklearn.svm import SVR
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d import Axes3D


class OrderRecommender:
    """
    Recommendations based on an expectation vs reality where the expectation is the expected number of shared stars and
    the reality is the actual number of shared stars. This returns the ratio between the two.
    """

    def __init__(self, db, watchers=False):
        positions = {}

        sorting_criteria = 'scraped_watchers' if watchers else 'scraped_stars'

        for i, repo in enumerate(db.repos.find().sort([(sorting_criteria, pymongo.DESCENDING)])):
            r_id = repo['r_id']

            positions[r_id] = i

        self.positions = positions
        self.watchers = watchers
        self.db = db

        print("Done loading in the gazers by r_id for the ratio based recommendations.")

    def get_recommendations(self, repo_id):
        db = self.db
        final = {}

        sorting_criteria = 'w' if self.watchers else 's'
        for i, score in enumerate(db.scores.find({'a': repo_id}).sort([(sorting_criteria, pymongo.DESCENDING)])):
            b_id = score['b']

            if i > self.positions[b_id]:
                numer = (i - self.positions[b_id])
                denom = (db.repos.count() - self.positions[b_id])

                final[b_id] = -numer / denom
            else:
                numer = self.positions[b_id] - i
                denom = max(1, self.positions[b_id])

                final[b_id] = numer / denom

        return final


class SVRRecommender:
    def __init__(self, db):
        """
        Creates the Ensemble Recommender which combines several recommendation criteria specified in its parameters,
        in order to provide better recommendations.
        """

        recommenders = [OrderRecommender(db), OrderRecommender(db, watchers=True)]

        r_id_map = {}

        for repo in db.repos.find():
            r_id = repo['r_id']

            r_id_map[r_id] = repo

        self.r_id_map = r_id_map

        print("Done loading in the gazers by r_id for the ensembled recommendations.")

        self.svr = SVR()

        x = []
        y = []

        for repo in db.repos.find():
            r_id = repo['r_id']

            if not db.feedback.find_one({'a': repo['indexed_name']}):
                continue

            recs = [reco.get_recommendations(r_id) for reco in recommenders]

            for feedback in db.feedback.find({'a': repo['indexed_name']}):
                b_id = db.repos.find_one({'indexed_name': feedback['b']})['r_id']

                x.append([rec[b_id] for rec in recs])
                y.append(feedback['score'])

        print("Starting the training.")

        self.svr.fit(np.array(x), np.array(y))
        self.recommenders = recommenders

        self.db = db

        print("Done training the SVR.")

    def get_recommendations(self, search_id):
        """
        :param search_id: the r_id for the desired repo.
        :return: a sorted list of results with tuples formatted as (score, other_repo's r_id)
        """

        recommenders = self.recommenders

        recs = [reco.get_recommendations(search_id) for reco in recommenders]

        final = []

        db = self.db

        for score in db.scores.find({'a': search_id}):
            b_id = score['b']

            if b_id == search_id:
                continue

            final.append((self.svr.predict([rec[b_id] for rec in recs]), b_id))

        final.sort(key=lambda tup: tup[0], reverse=True)

        return final


if __name__ == '__main__':
    client = MongoClient()
    db = client.kare
    reco = SVRRecommender(db)

    while True:
        repo = raw_input("Repo?")
        search_id = db.repos.find_one({'indexed_name': repo})['r_id']

        recos = reco.get_recommendations(search_id)[0:10]
        print(recos)

        results = [db.repos.find_one({'r_id': tup[1]}) for tup in recos]
        print(results)

        grade = raw_input("grade?") in ['y', 'yes']

        if not grade:
            continue

        for result in results:

            score = int(raw_input("%s: %s | " % (result['name'], result['description'])))

            db.feedback.insert({'a': repo, 'b': result['indexed_name'], 'score': score})
