package io.kare.suggest.stars;

import com.mongodb.*;

/**
 * @author arshsab
 * @since 03 2014
 */

public class CopyOverStarsAlgorithm {
    public void copy(DB from, DB to) {
        DBCollection fromStars = from.getCollection("stars");
        DBCollection toStars   = to.getCollection("stars");

        // Ensure that we don't have duplicate entries.
        toStars.ensureIndex(new BasicDBObject()
            .append("gazer", 1)
            .append("name", 1),
                new BasicDBObject()
            .append("unique", true)
        );

        fromStars.find().forEach((repo) -> {
            try {
                toStars.insert(repo);
            } catch (MongoException ignored) {}
        });

        toStars.ensureIndex(new BasicDBObject("gazer", 1));
        toStars.ensureIndex(new BasicDBObject("name", 1));
    }
}
