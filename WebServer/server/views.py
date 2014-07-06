from flask import json, abort, render_template
from server import app
import auto, reco, mongo

@app.route('/')
@app.route('/index')
def render_index():
    return render_template('index.html')


@app.route('/search/<path:query>')
def render_results(query):
    query = auto.fixup_query(query)

    if query is None:
        abort(404)

    query = query.lower()

    recommendations = reco.recommend(query)

    return render_template('results.html', recommendations=recommendations[0:20], current_repo=query)

@app.route('/about')
def render_about():
    db = mongo.get_db()
    repo_count = db.repos.count()

    return render_template('about.html', repo_count=repo_count)

@app.route('/auto/<query>')
@app.route('/auto/<query>/')
@app.route('/auto/<owner>/<repo>')
def render_auto(query=None, owner=None, repo=None):
    if query is None:
        query = owner + '/' + repo
    else:
        query = query.replace("%2f", "/")
        query = query.replace("%2F", "/")

    query = query.lower()

    return json.jsonify(results=auto.autocomplete(query))


@app.errorhandler(404)
def render_404(error):
    return render_template('404.html'), 404
