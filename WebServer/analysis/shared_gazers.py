from pymongo import MongoClient


def main():
    client = MongoClient()

    db = client.kare

    repoA = raw_input('First Repo:')
    repoB = raw_input('Second Repo: ')

    gazers = set()

    for gazer in db.stars.find({'name': repoA}):
        gazers.add(gazer['gazer'])

    count = 0

    for gazer in db.stars.find({'name': repoB}):
        if gazer['gazer'] in gazers:
            count += 1

    print(count)

if __name__ == '__main__':
    main()
