package io.kare.suggest.tokens;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import io.kare.suggest.Logger;
import io.kare.suggest.tasks.Task;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author arshsab
 * @since 07 2014
 */

public class TokenAnalysisTask extends Task<UpdateTokenResult, Void> {
    private final DBCollection stars, repos, watchers, scores;
    private final Map<String, Integer> repoIds = new ConcurrentHashMap<>();
    private final Map<Integer, Integer> starsByIds = new ConcurrentHashMap<>();

    public TokenAnalysisTask(DBCollection stars, DBCollection watchers, DBCollection repos, DBCollection scores) {
        super(1, "Token Analysis");

        this.stars = stars;
        this.repos = repos;
        this.watchers = watchers;
        this.scores = scores;

        stars.ensureIndex(new BasicDBObject("name", 1));
        stars.ensureIndex(new BasicDBObject("gazer", 1));

        watchers.ensureIndex(new BasicDBObject("name", 1));
        watchers.ensureIndex(new BasicDBObject("gazer", 1));
    }


    private static final BasicDBObject starUpdate = new BasicDBObject("$inc", new BasicDBObject("s", 1));
    private static final BasicDBObject watcherUpdate = new BasicDBObject("$inc", new BasicDBObject("w", 1));
    @Override
    protected void consume(UpdateTokenResult result) {
        Logger.info("Processing the new tokens for: " + result.repo);

        HashSet<String> myGazers = gazersFor(result.repo);
        HashSet<String> myWatchers = watchersFor(result.repo);
        List<BasicDBObject> newStars = result.tokens.get(Token.STARGAZERS);

        // Reverse in case we crash in here.
        for (int i = newStars.size() - 1; i >= 0; i--) {
            String gazer = newStars.get(i).getString("gazer");
            myGazers.add(gazer);

            for (String otherRepo : starredBy(gazer)) {
                int aId = idFor(result.repo);
                int bId = idFor(otherRepo);

                BasicDBObject forwardQuery = new BasicDBObject()
                                                .append("a", aId)
                                                .append("b", bId);

                BasicDBObject backwardQuery = new BasicDBObject()
                                                .append("a", bId)
                                                .append("b", aId);


                scores.update(forwardQuery, starUpdate);
                scores.update(backwardQuery, starUpdate);

                BasicDBObject forward = (BasicDBObject) scores.findOne(forwardQuery);
                BasicDBObject backward = (BasicDBObject) scores.findOne(backwardQuery);


                if (forward == null || backward == null) {
                    int sharedStars, sharedWatchers;

                    if (backward != null) {
                        sharedStars = backward.getInt("s");
                        sharedWatchers = backward.getInt("w");
                    } else if (forward != null) {
                        sharedStars = forward.getInt("s");
                        sharedWatchers = forward.getInt("w");
                    } else {
                        sharedStars = sizeOfUnion(myGazers, gazersFor(otherRepo));
                        sharedWatchers = sizeOfUnion(myWatchers, watchersFor(otherRepo));
                    }

                    if (forward == null && sharedStars > computeThreshold(countGazersFor(aId))) {
                        forwardQuery.append("s", sharedStars)
                                    .append("w", sharedWatchers);

                        scores.insert(forwardQuery);
                    }

                    if (backward == null && sharedStars > computeThreshold(countGazersFor(bId))) {
                        backwardQuery.append("s", sharedStars)
                                     .append("w", sharedWatchers);

                        scores.insert(backwardQuery);
                    }
                }
            }

            stars.insert(newStars.get(i));
        }

        List<BasicDBObject> newWatchers = result.tokens.get(Token.WATCHERS);

        // Reverse in case we crash in here.
        for (int i = newWatchers.size() - 1; i >= 0; i--) {
            BasicDBObject watcher = newWatchers.get(i);

            for (String otherRepo : watchedBy(watcher.getString("gazer"))) {
                int aId = idFor(result.repo);
                int bId = idFor(otherRepo);

                BasicDBObject forwardQuery = new BasicDBObject()
                        .append("a", aId)
                        .append("b", bId);

                BasicDBObject backwardQuery = new BasicDBObject()
                        .append("a", bId)
                        .append("b", aId);


                scores.update(forwardQuery, watcherUpdate);
                scores.update(backwardQuery, watcherUpdate);
            }

            watchers.insert(watcher);
        }

        Logger.info("Finished processing the new tokens for: " + result.repo);

        markRepoCompleted(result.repo);
    }

    private void markRepoCompleted(String repo) {
        BasicDBObject obj = (BasicDBObject) repos.findOne(new BasicDBObject("indexed_name", repo));
        obj.put("scraped_stars", (int) stars.count(new BasicDBObject("name", repo)));

        BasicDBObject progress = (BasicDBObject) obj.get("progress");
        progress.put("stars_done", true);

        repos.save(obj);
    }

    private int computeThreshold(int stars) {
        return (int) Math.ceil(Math.pow(stars, 1 / 3.0));
    }

    private int countGazersFor(String repo) {
        return countGazersFor(idFor(repo));
    }

    private int countGazersFor(int id) {
        Integer ret = starsByIds.get(id);

        if (ret != null) {
            return ret;
        }

        BasicDBObject obj = (BasicDBObject) repos.findOne(new BasicDBObject("r_id", id));
        starsByIds.put(id, (ret = obj.getInt("gazers")));

        return ret;
    }

    private int idFor(String repo) {
        Integer ret = repoIds.get(repo);

        if (ret != null) {
            return ret;
        }

        ret = (Integer) repos.findOne(new BasicDBObject("indexed_name", repo)).get("r_id");
        repoIds.put(repo, ret);

        return ret;
    }

    private HashSet<String> starredBy(String gazer) {
        HashSet<String> result = new HashSet<>();

        for (DBCursor cursor = stars.find(new BasicDBObject("gazer", gazer)); cursor.hasNext(); ) {
            BasicDBObject star = (BasicDBObject) cursor.next();

            result.add(star.getString("name"));
        }

        return result;
    }

    private HashSet<String> watchedBy(String gazer) {
        HashSet<String> result = new HashSet<>();

        for (DBCursor cursor = watchers.find(new BasicDBObject("gazer", gazer)); cursor.hasNext(); ) {
            BasicDBObject watcher = (BasicDBObject) cursor.next();

            result.add(watcher.getString("name"));
        }

        return result;
    }

    private HashSet<String> watchersFor(String watcher) {
        HashSet<String> result = new HashSet<>();

        for (DBCursor cursor = watchers.find(new BasicDBObject("gazer", watcher)); cursor.hasNext(); ) {
            BasicDBObject watch = (BasicDBObject) cursor.next();

            result.add(watch.getString("gazer"));
        }

        return result;
    }

    private HashSet<String> gazersFor(String repo) {
        HashSet<String> result = new HashSet<>();

        for (DBCursor cursor = stars.find(new BasicDBObject("name", repo)); cursor.hasNext(); ) {
            BasicDBObject star = (BasicDBObject) cursor.next();

            result.add(star.getString("gazer"));
        }

        return result;
    }

    private int sizeOfUnion(HashSet<String> setA, HashSet<String> setB) {
        int count = 0;

        for (String s : setA) {
            if (setB.contains(s)) {
                count++;
            }
        }

        return count;
    }
}
