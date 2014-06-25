package io.kare.suggest;

import com.mongodb.*;

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

        String version = System.getProperty("kare.version");

        if (meta.getCount() == 0) {
            init(db);
        } else if (find(meta, "role", "version") == null ||
                !find(meta, "role", "version").getString("value").equals(version)) {

            init(db);
        } else if (find(meta, "role", "done").getBoolean("value")) {
            init(db);
        } else {
            BasicDBObject obj = find(meta, "role", "crashes");

            int prev = obj.getInt("value");

            obj.put("value", prev + 1);

            meta.save(obj);
        }

        if (find(meta, "role", "current_task").getString("value").equals("repo_updates")) {
            RepoUpdateAlgorithm.update(fetcher, db.getCollection("repos"), meta);

            BasicDBObject task = find(meta, "role", "current_task");
            task.put("value", "star_updates");

            meta.save(task, WriteConcern.JOURNALED);
        }

        if (find(meta, "role", "current_task").getString("value").equals("star_updates")) {
            UpdateStarsAlgorithm starAlgo = new UpdateStarsAlgorithm(db.getCollection("stars"),
                    db.getCollection("repos"),  db.getCollection("meta"), fetcher);

            for (DBObject obj : db.getCollection("repos").find()) {
                starAlgo.consume((BasicDBObject) obj);
            }

            starAlgo.completeProcessing();

            BasicDBObject task = find(meta, "role", "current_task");
            task.put("value", "correlation_updates");

            meta.save(task, WriteConcern.JOURNALED);
        }

        if (find(meta, "role", "current_task").getString("value").equals("correlation_updates")) {
            CorrelationsAlgorithm.correlate(db.getCollection("stars"), db.getCollection("repos"), db.getCollection("scores"), meta);

            BasicDBObject task = find(meta, "role", "current_task");
            task.put("value", "cleanup");

            meta.save(task, WriteConcern.JOURNALED);
        }

        BasicDBObject allDone = find(meta, "role", "done");
        allDone.put("value", true);

        meta.save(allDone);
    }

    public void init(DB db) {
        Logger.important("Resetting DB's update info.");

        db.getCollection("meta").drop();
        db.createCollection("meta", null);

        DBCollection meta = db.getCollection("meta");

        meta.insert(new BasicDBObject("role", "current_task").append("value", "repo_updates"));
        meta.insert(new BasicDBObject("role", "version").append("value", "1.0"));
        meta.insert(new BasicDBObject("role", "redos").append("value", 0));
        meta.insert(new BasicDBObject("role", "stars_done").append("value", 0));
        meta.insert(new BasicDBObject("role", "correlations_done").append("value", 0));
        meta.insert(new BasicDBObject("role", "done").append("value", false));
        meta.insert(new BasicDBObject("role", "crashes").append("value", 0));
        meta.insert(new BasicDBObject("role", "version").append("value", System.getProperty("kare.version")));
    }

    private BasicDBObject find(DBCollection coll, String what, String value) {
        return ((BasicDBObject) coll.findOne(new BasicDBObject(what, value)));
    }
}
