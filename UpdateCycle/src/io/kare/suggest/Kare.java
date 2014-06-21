package io.kare.suggest;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
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
                "scores",
                "meta"
        };

        Arrays.stream(collections)
                .filter(s -> !db.collectionExists(s))
                .forEach(s -> db.createCollection(s, null));

        DBCollection meta = db.getCollection("meta");

        if (meta.getCount() == 0) {
            init(db);
        }

        if (find(meta, "role", "AllDone").getBoolean("value")) {
            init(db);
        }

        meta = db.getCollection("meta");

        if (!find(meta, "role", "RepoUpdates").getBoolean("value"))
            RepoUpdateAlgorithm.update(fetcher, db.getCollection("repos"));

        BasicDBObject repoUpdates = find(meta, "role", "RepoUpdates");
        repoUpdates.put("value", true);

        meta.save(repoUpdates);

        if (!find(meta, "role", "StarUpdates").getBoolean("value")) {
            UpdateStarsAlgorithm starAlgo = new UpdateStarsAlgorithm(db.getCollection("stars"), db.getCollection("repos"), fetcher);

            for (DBObject obj : db.getCollection("repos").find()) {
                starAlgo.consume((BasicDBObject) obj);
            }

            starAlgo.completeProcessing();
        }

        BasicDBObject starUpdates = find(meta, "role", "StarUpdates");
        starUpdates.put("value", true);

        meta.save(starUpdates);

        if (!find(meta, "role", "Correlations").getBoolean("value"))
            CorrelationsAlgorithm.correlate(db.getCollection("stars"), db.getCollection("repos"), db.getCollection("scores"));

        BasicDBObject correlations = find(meta, "role", "Correlations");
        correlations.put("value", true);

        meta.save(correlations);

        BasicDBObject allDone = find(meta, "role", "AllDone");
        allDone.put("value", true);

        meta.save(allDone);
    }

    public void init(DB db) {
        db.getCollection("meta").drop();
        db.createCollection("meta", null);

        DBCollection meta = db.getCollection("meta");

        meta.insert(new BasicDBObject("role", "RepoUpdates").append("value", false));
        meta.insert(new BasicDBObject("role", "StarUpdates").append("value", false));
        meta.insert(new BasicDBObject("role", "Correlations").append("value", false));
        meta.insert(new BasicDBObject("role", "AllDone").append("value", false));
    }

    private BasicDBObject find(DBCollection coll, String what, String value) {
        return ((BasicDBObject) coll.findOne(new BasicDBObject(what, value)));
    }
}
