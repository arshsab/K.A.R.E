from pymongo import MongoClient

client = MongoClient()
db = client.kare

if __name__ == '__main__':
    repo_A = raw_input("Enter Repo 1: ").lower()
    repo_B = raw_input("Enter Repo 2: ").lower()

    gazers_A = set()

    for star in db.stars.find({'name': repo_A}):
        gazers_A.add(star['gazer'])

    count = 0

    for star in db.stars.find({'name': repo_B}):
        if star['gazer'] in gazers_A:
            count += 1

    print(count)