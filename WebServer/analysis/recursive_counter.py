from collections import Counter
from webserver.data.recommenders import SimpleRecommender


def find(list, name):
    for obj in list:
        if obj['other'] == name:
            return obj

    return None


def get_recommendations(repo_name):
    recommender = SimpleRecommender()

    objects = recommender.detailed_recommendations(repo_name, 10)

    print([o['other'] for o in objects])
    # print([o['adjusted_score'] for o in objects])

    for obj in objects:
        objects2 = recommender.detailed_recommendations(obj['other'], 10)

        other = find(objects2, obj['repo'])

        if other is not None:
            obj['adjusted_score'] += other['adjusted_score']

    objects.sort(key=lambda o: o['adjusted_score'], reverse=True)

    print([o['other'] for o in objects])
    # print([o['adjusted_score'] for o in objects])

if __name__ == '__main__':
    repo_name = raw_input('Repo Name: ')

    get_recommendations(repo_name)