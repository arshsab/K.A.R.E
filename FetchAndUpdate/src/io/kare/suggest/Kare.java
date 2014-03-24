package io.kare.suggest;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import io.kare.suggest.fetch.Fetcher;
import io.kare.suggest.repos.OutOfDateRepoIdentificationAlgorithm;
import io.kare.suggest.repos.RepoUpdateAlgorithm;
import io.kare.suggest.stars.CopyOverStarsAlgorithm;
import io.kare.suggest.stars.CorrelationsAlgorithm;
import io.kare.suggest.stars.UpdateStarsRunnable;

import java.io.IOException;
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

    Kare() {
        this.fetcher = new Fetcher(System.getProperty("api-key"));
    }

    public void update(DB from,  DB to) throws IOException {
        to.createCollection("repos", null);
        to.createCollection("stars", null);
        to.createCollection("correlations", null);

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

        Logger.important("Loading in all the old stars.");
        new CopyOverStarsAlgorithm().copy(from, to);
        Logger.important("Finished loading in all the old stars.");

        Logger.important("Starting the Correlations algorithm.");
        new CorrelationsAlgorithm(to).correlate();
        Logger.important("Finished the Correlations algorithm.");
    }
}
