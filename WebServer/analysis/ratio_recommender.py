from __future__ import division
from collections import Counter
import math
from pymongo import MongoClient

__author__ = 'Me'

class RatioRecommender:
    def __init__(self):
        client = MongoClient()
        self.db = client.kare
        self.counts = {}

        for repo in self.db.repos.find():
            self.counts[repo['indexed_name']] = repo['gazers']

        self.total = 756226

    def get_recommendations_for(self, repo_name):
        db = self.db

        cnt = Counter()

        for gazer in db.stars.find({'name': repo_name}):
            for other in db.stars.find({'gazer': gazer['gazer']}):
                cnt[other['name']] += 1


        final = []

        count = 0

        for k, v in cnt.iteritems():
            if k not in self.counts:
                continue

            if v <= math.ceil(self.counts[repo_name] ** (1 / 3)):
                continue

            expected = self.counts[k] / self.total * self.counts[repo_name]

            ratio = v / expected

            final.append((ratio, v, k))

            count += 1

        final.sort(key=lambda tup: tup[0], reverse=True)

        print(count)

        return final[1:1000]


if __name__ == '__main__':
    reco = RatioRecommender()

    print(reco.get_recommendations_for(raw_input('Repo: ').lower()))