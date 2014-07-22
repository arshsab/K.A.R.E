package model;

import com.mongodb.*;
import play.Logger;

import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author arshsab
 * @since 07 2014
 */

public class Model {
    private final MongoClient client;
    private final DB db;
    private final Map<String, Integer> forwardRids = new ConcurrentHashMap<>();
    private final Map<Integer, String> backwardRids = new ConcurrentHashMap<>();
    private final Map<String, Repo> repoLookup = new ConcurrentHashMap<>();
    public final DBCollection repos,
                              stars,
                              watchers,
                              feedback,
                              scores,
                              cachedRecs;
    public final Recommender reco;
    public final AutoCompleter auto;
    public final Statistics stats;

    public Model() {
        try {
            client = new MongoClient();
            db = client.getDB("kare");

            repos      = db.getCollection("repos");
            stars      = db.getCollection("stars");
            watchers   = db.getCollection("watchers");
            feedback   = db.getCollection("feedback");
            scores     = db.getCollection("scores");
            cachedRecs = db.getCollection("cached_recs");

            // change based on what type of recommender to use
//            reco = new OrderRecommender(this);
            reco = new CachedRecommender(this);
            auto = new AutoCompleter(this);;

            stats = new Statistics(this);

            Logger.info("Model is setup...");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public String lookup(int rId) {
        if (backwardRids.containsKey(rId)) {
            return backwardRids.get(rId);
        }

        BasicDBObject obj = (BasicDBObject) repos.findOne(new BasicDBObject("r_id", rId));
        updateStores(obj);

        return lookup(rId);
    }

    public int lookup(String repo) {
        repo = repo.toLowerCase();

        if (forwardRids.containsKey(repo)) {
            return forwardRids.get(repo);
        }

        BasicDBObject obj = (BasicDBObject) repos.findOne(new BasicDBObject("indexed_name", repo));

        if (obj == null) {
            return -1;
        }

        updateStores(obj);

        return lookup(repo);
    }

    public boolean exists(String repo) {
        return lookup(repo) != -1;
    }

    public Repo getRepo(int rId) {
        return getRepo(lookup(rId));
    }

    public Repo getRepo(String repoName) {
        lookup(repoName);

        return repoLookup.get(repoName);
    }

    private synchronized void updateStores(BasicDBObject obj) {
        Repo r = new Repo(obj);

        forwardRids.put(r.indexedName, r.rId);
        backwardRids.put(r.rId, r.indexedName);

        repoLookup.put(r.indexedName, r);
    }
}
