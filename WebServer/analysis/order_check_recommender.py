from collections import Counter
from math import ceil
from pymongo import MongoClient


class ByOrderRecommender:
    def __init__(self):
        client = MongoClient()
        self.db = client.kare

        repos = []

        for repo in self.db.repos.find():
            repos.append((repo['indexed_name'], repo['gazers']))

        repos.sort(key=lambda tup: tup[1], reverse=True)

        self.size = len(repos)
        self.positions = dict((repo[0], i) for i, repo in zip(range(0, len(repos)), repos))

    def get_recommendations_for(self, repo_name):
        db = self.db

        cnt = Counter()

        i = 0
        for gazer in db.stars.find({'name': repo_name}):
            for other in db.stars.find({'gazer': gazer['gazer']}):
                cnt[other['name']] += 1

            if i % 100 == 0:
                print("Done with %d gazers." % i)

            i += 1

        in_order = [(repo, count) for repo, count in cnt.iteritems()]
        in_order.sort(key=lambda tup: tup[1], reverse=True)

        print(len(in_order))
        print(in_order[1:1000])

        cnt2 = Counter()

        for k, v in cnt.iteritems():
            cnt2[v] += 1

        print(cnt2)

        final = []

        my_stars = db.repos.find_one({'indexed_name': repo_name})['gazers']
        limit = ceil(my_stars ** (1 / float(3)))

        count = 0
        for repo, i in zip(in_order, range(0, len(in_order))):
            stars = repo[1]
            repo = repo[0]

            if repo not in self.positions:
                continue

            if stars > limit:
                count += 1

            numer = self.positions[repo] - i
            denom = None

            if numer > 0:
                denom = self.positions[repo] + 1
            else:
                denom = self.size - self.positions[repo]

            final.append((numer / float(denom), numer, denom, stars, repo))

        final.sort(key=lambda tup: tup[0], reverse=True)

        print(count)

        return final[1:1000]


if __name__ == '__main__':
    reco = ByOrderRecommender()

    print(reco.get_recommendations_for(raw_input('Repo: ').lower()))