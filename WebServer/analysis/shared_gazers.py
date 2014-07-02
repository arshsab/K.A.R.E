from pymongo import MongoClient


def main():
    client = MongoClient()

    db = client.ktest

    repoA = raw_input('First Repo:').lower()
    repoB = raw_input('Second Repo: ').lower()

    gazers = set()

    for gazer in db.watchers.find({'name': repoA}):
        gazers.add(gazer['gazer'])

    count = 0

    for gazer in db.watchers.find({'name': repoB}):
        if gazer['gazer'] in gazers:
            count += 1

    print(count)

if __name__ == '__main__':
    main()
