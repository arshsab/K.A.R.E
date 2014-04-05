package io.kare.suggest;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;

import io.kare.suggest.fetch.Fetcher;
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
        this.fetcher = new Fetcher();
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


        RepoConsumer[] consumers = {
                new UpdateStarsAlgorithm(db.getCollection("stars"), db.getCollection("repos"), fetcher)
        };

        RepoUpdateAlgorithm.update(fetcher, db.getCollection("repos"), consumers);

        CorrelationsAlgorithm.correlate(db.getCollection("stars"), db.getCollection("repos"), db.getCollection("scores"));
    }

    public void init(DB db) {
        if (!db.collectionExists("meta")) {
            db.createCollection("meta", null);
            db.getCollection("meta").insert(new BasicDBObject("role", "since").append("value", 0));
        }
    }
}
