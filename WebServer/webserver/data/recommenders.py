from . import scores
import pymongo
from .models import Repo


class SimpleRecommender:
    def __init__(self):
        pass

    def recommendations_for(self, repo_name):
        cursor = scores.find({
            'repo': repo_name
        }).sort([
            ('adjusted_score', pymongo.DESCENDING)
        ])

        recommendations = []

        i = 0
        for reco in cursor:
            if i == 20:
                break

            i += 1

            recommendations.append(Repo(reco['other']))

        return recommendations
