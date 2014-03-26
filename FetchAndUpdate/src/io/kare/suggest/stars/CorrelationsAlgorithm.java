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
    public static void correlate(DBCollection stars, DBCollection updates, DBCollection scores) {
        Logger.important("Rebuilding correlations for repos that need updates.");

        int completed = 0;

        DBCursor repoCursor = updates.find();
        repoCursor.addOption(Bytes.QUERYOPTION_NOTIMEOUT);

        while (repoCursor.hasNext()) {
            BasicDBObject repo = (BasicDBObject) repoCursor.next();

            Map<String, Integer> correlations = new HashMap<>(20_000);

            Logger.debug("Starting with repo: " + repo.getString("repo"));

            DBCursor gazerCursor = stars.find(new BasicDBObject("name", repo.getString("repo")));
            while (gazerCursor.hasNext()) {
                BasicDBObject gazer = (BasicDBObject) gazerCursor.next();

                Logger.debug("Found gazer for " + repo.getString("repo") + " named " + gazer.getString("gazer"));

                DBCursor correlationCursor = stars.find(new BasicDBObject("gazer", gazer.getString("gazer")));
                while (correlationCursor.hasNext()) {
                    BasicDBObject correlation = (BasicDBObject) correlationCursor.next();

                    String otherRepo = correlation.getString("name");

                    if (!otherRepo.equals(repo.getString("repo"))) {
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

                String thisRepo = repo.getString("repo");
                String otherRepo = nex.getValue();

                int score = nex.getKey();

                scores.insert(new BasicDBObject()
                        .append("repo", thisRepo)
                        .append("other", otherRepo)
                        .append("score", score)
                );
            }

            Logger.info("Correlations for: #" + ++completed + " (" + repo.getString("name") + ") are done.");
        }
        
        Logger.important("Finished rebuilding correlations for repos that needed update.");
    }
}
