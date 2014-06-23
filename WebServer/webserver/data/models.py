from . import repos


class Repo:
    def __init__(self, repo_name, star_count=None, language=None, description=None):
        repo_name = repo_name.lower()

        if None in [star_count, language, description]:
            repo = repos.find_one(dict(indexed_name=repo_name))

            if repo is not None:
                star_count = repo['gazers']
                language = repo['language']
                description = repo['description']

        self.name = repo_name
        self.stars = star_count
        self.language = language
        self.description = description


class Recommendation:
    def __init__(self, repo_one, repo_two, score):
        self.repo_one = repo_one
        self.repo_two = repo_two
        self.score = score