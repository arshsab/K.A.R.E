package io.kare.suggest.stars;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import io.kare.suggest.Logger;
import io.kare.suggest.RepoConsumer;
import io.kare.suggest.fetch.Fetcher;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author arshsab
 * @since 03 2014
 */

public class UpdateStarsAlgorithm implements RepoConsumer {
    private static final int STAR_UPDATE_THRESHOLD;

    static {
        String prop = System.getProperty("kare.repos.star-update-threshold");

        STAR_UPDATE_THRESHOLD = prop == null ? 25 : Integer.parseInt(prop);
    }

    private final DBCollection stars;
    private final Fetcher fetcher;
    private final ExecutorService exec = Executors.newFixedThreadPool(8);
    private final DBCollection repos;

    public UpdateStarsAlgorithm(DBCollection stars, DBCollection repos, Fetcher fetcher) {
        this.stars = stars;
        this.fetcher = fetcher;
        this.repos = repos;

        stars.ensureIndex(new BasicDBObject()
                .append("repo", 1)
                .append("gazer", 1),
                new BasicDBObject()
                        .append("unique", true)
                        .append("dropDups", true)
        );
    }

    @Override
    public void consume(BasicDBObject repo) throws IOException {
        final int alreadyScraped = repo.getInt("scraped_stars");
        final int totalStars = repo.getInt("gazers");

        if (totalStars - alreadyScraped >= STAR_UPDATE_THRESHOLD) {
            exec.submit(new UpdateStarsRunnable(stars, fetcher, repo.getString("indexed_name"), 0,
                    (int) Math.ceil((totalStars - alreadyScraped) / 100.0)));

            repo.put("scraped_stars", totalStars);

            repos.save(repo);
        }
    }

    @Override
    public void completeProcessing() {
        exec.shutdown();
        while (!exec.isTerminated()) {
            try {
                exec.awaitTermination(1000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
