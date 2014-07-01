package io.kare.suggest;

import com.mongodb.*;

import io.kare.suggest.fetch.Fetcher;
import io.kare.suggest.repos.RepoUpdateTask;
import io.kare.suggest.tokens.UpdateTokensTask;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author arshsab
 * @since 03 2014
 */

public class Kare {
    private final Fetcher fetcher;

    Kare() {
        this.fetcher = new Fetcher();
    }

    public void update(DB db) throws IOException, InterruptedException {
        String[] collections = {
                "repos",
                "stars",
                "watchers",
                "updates",
                "scores",
                "meta"
        };

        Arrays.stream(collections)
                .filter(s -> !db.collectionExists(s))
                .forEach(s -> db.createCollection(s, null));

        DBCollection meta = db.getCollection("meta");
        DBCollection repos = db.getCollection("repos");

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

        RepoUpdateTask repoUpdates = new RepoUpdateTask(fetcher, repos, meta);
        UpdateTokensTask tokenUpdates = new UpdateTokensTask(db, repos, meta, new AtomicInteger(), fetcher);
        // todo :

        repoUpdates.addConsumer(tokenUpdates);
        repoUpdates.startChain();

        repoUpdates.awaitShutdown();

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
