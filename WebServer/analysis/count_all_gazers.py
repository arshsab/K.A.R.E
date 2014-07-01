from pymongo import MongoClient

__author__ = 'Me'

def main():
    client = MongoClient()

    db = client.kare

    uniq = set()

    i = 0

    for star in db.stars.find():
        uniq.add(star['gazer'])

        if i % 10000 == 0:
            print("Done with: %d" % i)

        i += 1

    print(len(uniq))

if __name__ == '__main__':
    main()