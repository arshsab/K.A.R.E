package io.kare.suggest;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;

import io.kare.suggest.fetch.Fetcher;
import io.kare.suggest.repos.OutOfDateRepoIdentificationAlgorithm;
import io.kare.suggest.repos.RepoUpdateAlgorithm;
import io.kare.suggest.stars.CorrelationsAlgorithm;
import io.kare.suggest.stars.UpdateStarsAlgorithm;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author arshsab
 * @since 03 2014
 */

public class Kare {
    private final Fetcher fetcher;

    Kare() {
        Objects.requireNonNull(System.getProperty("kare.api-key"),
                "System property: kare.api-key cannot be null");

        this.fetcher = new Fetcher(System.getProperty("kare.api-key"));
    }

    public void update(DB db) throws IOException {
        String[] collections = {
                "repos",
                "stars",
                "updates",
                "scores"
        };

        Arrays.stream(collections)
              .filter(s -> !db.collectionExists(s))
              .forEach(s -> db.createCollection(s, null));


        DBCollection meta = db.getCollection("meta");
        BasicDBObject sinceDBObject = (BasicDBObject) meta.findOne(new BasicDBObject("role", "since"));

        int newSince = RepoUpdateAlgorithm.update(fetcher, db.getCollection("repos"), sinceDBObject.getInt("value"));

        meta.update(new BasicDBObject("role", "since"), new BasicDBObject("role", "since").append("value", newSince));

        OutOfDateRepoIdentificationAlgorithm.identify(db.getCollection("repos"), db.getCollection("updates"), fetcher);

        UpdateStarsAlgorithm.update(db.getCollection("updates"), db.getCollection("stars"), fetcher);

        CorrelationsAlgorithm.correlate(db.getCollection("stars"), db.getCollection("repos"), db.getCollection("scores"));
    }

    public void init(DB db) {
        if (!db.collectionExists("meta")) {
            db.createCollection("meta", null);
            db.getCollection("meta").insert(new BasicDBObject("role", "since").append("value", 1));
        }
    }
}
