from pymongo import MongoClient

__all__ = [
    'Repo',
    'Recommendation',
    'SimpleRecommender'
]

def setup_mongo():
    client = MongoClient()
    db = client.kare

    return db.scores, db.stars, db.repos

scores, stars, repos = setup_mongo()