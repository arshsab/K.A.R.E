from __future__ import division

import numpy as np
from sklearn.svm import SVC


class WatchersRecommender:
    """
    Recommendations done purely by the number of watchers that are shared between two repos.
    """

    def __init__(self):
        pass

    def score_recommendation(self, score_obj):
        return score_obj['w']


class RatioRecommender:
    """
    Recommendations based on an expectation vs reality where the expectation is the expected number of shared stars and
    the reality is the actual number of shared stars. This returns the ratio between the two.
    """

    def __init__(self, db):
        all_gazers = set()

        for star in db.stars.find():
            all_gazers.add(star)

        self.star_counts = len(all_gazers)

        print("Done loading in the star sample size for the ratio based recommendations.")

        gazers_map = {}

        for repo in db.repos.find():
            r_id = repo['r_id']
            gazers = repo['scraped_stars']

            gazers_map[r_id] = gazers

        self.gazers_map = gazers_map

        print("Done loading in the gazers by r_id for the ratio based recommendations.")

    def score_recommendation(self, score_obj):
        b_id = score_obj['b']

        expected_gazers = self.gazers_map[b_id] / self.star_counts

        return score_obj['s'] / expected_gazers


class EnsembleRecommender:

    def __init__(self, db, recommenders):
        """
        Creates the Ensemble Recommender which combines several recommendation criteria specified in its parameters,
        in order to provide better recommendations.
        """
        r_id_map = {}

        for repo in db.repos.find():
            r_id = repo['r_id']

            r_id_map[r_id] = repo

        self.r_id_map = r_id_map

        print("Done loading in the gazers by r_id for the ensembled recommendations.")

        self.svc = SVC()

        x = []
        y = []

        for feedback in db.feedback.find():
            repo_a = db.repos.find_one({'indexed_name': feedback['a']})
            repo_b = db.repos.find_one({'indexed_name': feedback['b']})

            score_obj = db.scores.find_one({'a': repo_a['r_id'], 'b': repo_b['r_id']})

            x.append([reco.score_recommendation(score_obj) for reco in recommenders])

            y.append(feedback['score'])

        self.svc.fit(np.array(x), np.array(y))
        self.recommenders = recommenders

    def get_recommendations(self, score_obj):
        return self.svc.predict([[reco.score_recommendation(score_obj) for reco in self.recommenders]])[0]


def get_recommendations(recommender, db, r_id):
    """
    Gives recommendations for a repo given the database, the desired recommender and the repo_name

    :param recommender: The desired recommender.
    :param db: The mongo database
    :param r_id: The repo's r_id
    :return: A list of repos sorted from most recommended to least in the form (score, r_id)
    """

    scores = []

    for score in db.scores.find({'a': r_id}):
        scores.append((recommender.get_recommendations(score), score['b']))

    scores.sort(key=lambda tup: tup[0], reverse=True)

    return scores