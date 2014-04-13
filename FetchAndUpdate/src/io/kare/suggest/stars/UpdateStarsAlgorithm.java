package io.kare.suggest.stars;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
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
                .append("name", 1)
                .append("gazer", 1),
                new BasicDBObject()
                        .append("unique", true)
                        .append("dropDups", true)
        );

        stars.ensureIndex(new BasicDBObject("name", 1));
        stars.ensureIndex(new BasicDBObject("gazer", 1));
    }

    @Override
    public void consume(BasicDBObject repo) throws IOException {
        final int alreadyScraped = repo.getInt("scraped_stars");
        final int totalStars = repo.getInt("gazers");

        if (repo.getBoolean("processable") && totalStars - alreadyScraped >= STAR_UPDATE_THRESHOLD) {
            exec.submit(new UpdateStarsRunnable(stars, repos, fetcher, repo));
        } else {
            repo.put("processable", false);
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
