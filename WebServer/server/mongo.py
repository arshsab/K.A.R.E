from pymongo import MongoClient
from flask import g
from server import app

_client = MongoClient()
_db = _client.kare


def get_db():
    if not hasattr(g, 'db'):
        g.db = _db

    return g.db


@app.teardown_appcontext
def close_db(error):
    _client.close()
