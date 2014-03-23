package io.kare.suggest.stars;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author arshsab
 * @since 03 2014
 */

public class CorrelationsAlgorithm {
    private final DB db;

    public CorrelationsAlgorithm(DB db) {
        this.db = db;
    }

    public void correlate() {
        DBCollection stars = db.getCollection("stars");
        DBCollection repos = db.getCollection("repos");
        DBCollection scores = db.getCollection("scores");

        DBCursor repoCursor = repos.find();
        while (repoCursor.hasNext()) {
            BasicDBObject repo = (BasicDBObject) repoCursor.next();

            Map<String, Integer> correlations = new HashMap<>(20_000);

            DBCursor gazerCursor = stars.find(new BasicDBObject("name", repo.getString("name")));
            while (gazerCursor.hasNext()) {
                BasicDBObject gazer = (BasicDBObject) gazerCursor.next();

                DBCursor correlationCursor = stars.find(new BasicDBObject("gazer", gazer.getString("gazer")));
                while (correlationCursor.hasNext()) {
                    BasicDBObject correlation = (BasicDBObject) correlationCursor.next();

                    String otherRepo = correlation.getString("name");

                    if (!otherRepo.equals(repo.getString("name"))) {
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

                String thisRepo = repo.getString("name");
                String otherRepo = nex.getValue();

                int score = nex.getKey();

                scores.insert(new BasicDBObject()
                    .append("repo", thisRepo)
                    .append("other", otherRepo)
                    .append("score", score)
                );
            }
        }
    }
}
