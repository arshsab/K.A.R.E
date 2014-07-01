from pymongo import MongoClient


if __name__ == '__main__':
    client = MongoClient()

    db = client.test

    for i in range(0, 1000):
        db.coll.insert({'a': 1, 'b': 1, 'c': 1, 'd': 1})