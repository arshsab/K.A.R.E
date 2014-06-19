package io.kare.suggest.stars;

import com.mongodb.*;
import io.kare.suggest.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author arshsab
 * @since 03 2014
 */

public class CorrelationsAlgorithm {
    public static void correlate(DBCollection stars, DBCollection repos, DBCollection scores) {
        Logger.important("Rebuilding correlations for repos that need updates.");

        int completed = 0;

        scores.ensureIndex(new BasicDBObject("repo", 1));

        Map<String, Integer> starCounts = new HashMap<>();

        for (DBCursor cursor = repos.find(); cursor.hasNext(); ) {
            BasicDBObject repo = (BasicDBObject) cursor.next();

            starCounts.put(
                repo.getString("indexed_name"),
                repo.getInt("gazers")
            );
        }

        DBCursor repoCursor = repos.find();
        repoCursor.addOption(Bytes.QUERYOPTION_NOTIMEOUT);

        while (repoCursor.hasNext()) {
            BasicDBObject repo = (BasicDBObject) repoCursor.next();

            BasicDBObject progress = (BasicDBObject) repo.get("progress");

            if (progress.getBoolean("correlations_done")) {
                ++completed;
                continue;
            }

            scores.remove(new BasicDBObject("repo", repo.getString("indexed_name")));

            Map<String, Integer> correlations = new HashMap<>(20_000);

            Logger.debug("Starting with repo: " + repo.getString("indexed_name"));

            DBCursor gazerCursor = stars.find(new BasicDBObject("name", repo.getString("indexed_name")));
            while (gazerCursor.hasNext()) {
                BasicDBObject gazer = (BasicDBObject) gazerCursor.next();

                Logger.debug("Found gazer for " + repo.getString("indexed_name") + " named " + gazer.getString("gazer"));

                DBCursor correlationCursor = stars.find(new BasicDBObject("gazer", gazer.getString("gazer")));
                while (correlationCursor.hasNext()) {
                    BasicDBObject correlation = (BasicDBObject) correlationCursor.next();

                    String otherRepo = correlation.getString("name");

                    if (!otherRepo.equals(repo.getString("indexed_name"))) {
                        int nex = correlations.getOrDefault(otherRepo, 0);

                        correlations.put(otherRepo, nex + 1);
                    }
                }
            }

            // Reverse Order and allow duplicates.
            Map<Integer, String> sorted = new TreeMap<>((a, b) -> a.equals(b) ? 1 : b - a);

            correlations.forEach((name, score) -> sorted.put(score, name));

            Iterator<Map.Entry<Integer, String>> iter = sorted.entrySet().iterator();
            for (int i = 0; i < 1000 && iter.hasNext(); i++) {
                Map.Entry<Integer, String> nex = iter.next();

                String thisRepo = repo.getString("indexed_name");
                String otherRepo = nex.getValue();

                int score = nex.getKey();
                double adjustedScore = score / Math.sqrt(starCounts.get(otherRepo));

                scores.insert(new BasicDBObject()
                        .append("repo", thisRepo)
                        .append("other", otherRepo)
                        .append("score", score)
                        .append("adjusted_score", adjustedScore)
                );
            }

            Logger.info("Correlations for: #" + ++completed + " (" + repo.getString("name") + ") are done.");
        }
        
        Logger.important("Finished rebuilding correlations for repos that needed update.");
    }
}
