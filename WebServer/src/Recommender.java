import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author arshsab
 * @since 03 2014
 */

public class Recommender {
    private final DBCollection scores;
    private final DBCollection repos;

    public Recommender(DBCollection scores, DBCollection repos) {
        this.scores = scores;
        this.repos = repos;
    }

    public List<Recommendation> getLinearRecommendations(String repo) {
        List<Recommendation> ret = new ArrayList<>();

        System.out.println("Getting recommendations for: " + repo);
        System.out.printf("Recommendations have %d results.\n", scores.count(new BasicDBObject("repo", repo)));

        for (DBObject obj : scores.find(new BasicDBObject("repo", repo))) {
            BasicDBObject recommendation = (BasicDBObject) obj;

            String otherName = recommendation.getString("other");
            int score = recommendation.getInt("score");

            BasicDBObject otherRepo = (BasicDBObject) repos.findOne(new BasicDBObject("indexed_name", otherName));

            double corrected = score / Math.sqrt(otherRepo.getInt("gazers"));

            String language = otherRepo.getString("language");
            String description = otherRepo.getString("description");
            int gazers = otherRepo.getInt("gazers");

            ret.add(new Recommendation(repo, otherName, language, description, gazers, corrected));
        }

        Collections.sort(ret, (a, b) -> {
            double attempt = a.score - b.score;

            if (attempt == 0.0) {
                return 0;
            }

            if (attempt > 0.0) {
                return -1;
            }

            return 1;
        });

        ret = ret.subList(0, Math.min(ret.size(), 75));

        System.out.printf("Got %d results.", ret.size());

        return ret;
    }

    public List<Recommendation> getGraphRecommendations(String repo) {
        // todo

        return null;
    }
}
