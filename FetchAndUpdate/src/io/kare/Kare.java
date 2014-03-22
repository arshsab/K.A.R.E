package io.kare;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author arshsab
 * @since 03 2014
 */

public class Kare {
    private final Fetcher fetcher;
    private final ObjectMapper mapper = new ObjectMapper();

    Kare() {
        this.fetcher = new Fetcher(System.getProperty("api-key"));
    }

    public void update(DB from,  DB to) throws IOException {
        Logger.important("Starting a search for all repos.");
        new RepoUpdateAlgorithm(fetcher).update(to);
        Logger.important("Finished a search for all repos.");

        Logger.important("Scanning for out of date repos.");
        Map<String, Integer> outOfDateRepos = new OutOfDateRepoIdentificationAlgorithm(from, to).identify();
        Logger.important("Finished scanning for out of date repos.");

        Logger.important("Loading in all the new stars.");
        ExecutorService exec = Executors.newFixedThreadPool(10);

        DBCollection repos = to.getCollection("repos");
        for (Map.Entry<String, Integer> repo : outOfDateRepos.entrySet()) {
            int totalStars = ((BasicDBObject) repos.findOne(
                    new BasicDBObject("name", repo.getKey()))
            ).getInt("gazers");

            exec.submit(new UpdateStarsRunnable(to, fetcher, repo.getKey(),
                    (int) (totalStars - Math.ceil(repo.getValue() / 100.0)), totalStars));

            // Temporary. After one run like this, we will change the algorithm a little to load the newest stars rather
            // than the oldest stars.
        }

        exec.shutdown();
        while (!exec.isTerminated()) {
            try {
                exec.awaitTermination(60, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        Logger.important("Finished loading in all the new stars.");

        Logger.important("Starting the Correlations algorithm.");
        new CorrelationsAlgorithm(to).correlate();
        Logger.important("Finished the Correlations algorithm.");
    }
}
