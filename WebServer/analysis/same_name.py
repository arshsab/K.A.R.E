from collections import Counter
from pymongo import MongoClient


def main():
    client = MongoClient()

    db = client.kare

    names = Counter()

    for repo in db.repos.find():
        name = repo['indexed_name'].split("/")[1]

        names[name] += 1

    print(names.most_common(100))


if __name__ == '__main__':
    main()