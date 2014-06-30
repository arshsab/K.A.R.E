from . import scores
import pymongo
from .models import Repo


class SimpleRecommender:
    def __init__(self):
        pass

    def recommendations_for(self, repo_name):
        names = self.detailed_recommendations(repo_name, limit=20)

        recommendations = []
        i = 0

        for name in names:
            recommendations.append(Repo(name))

        return recommendations

    def detailed_recommendations(self, repo_name, limit=100):
        cursor = scores.find({
            'repo': repo_name
        }).sort([
            ('adjusted_score', pymongo.DESCENDING)
        ])

        recommendations = []
        i = 0

        for reco in cursor:
            if i > limit:
                break

            recommendations.append(reco)

        return recommendations