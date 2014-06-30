package io.kare.suggest.stars;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import io.kare.suggest.RepoConsumer;
import io.kare.suggest.fetch.Fetcher;
import io.kare.suggest.statistic.IncrementedStatistic;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
    private final DBCollection repos;
    private final IncrementedStatistic stat;
    private final Fetcher fetcher;
    private final ExecutorService exec = Executors.newFixedThreadPool(8);
    private final AtomicInteger atom = new AtomicInteger(0);

    public UpdateStarsAlgorithm(DBCollection stars, DBCollection repos, IncrementedStatistic stat, Fetcher fetcher) {
        this.fetcher = fetcher;
        this.stars = stars;
        this.repos = repos;
        this.stat = stat;

        stars.ensureIndex(
            new BasicDBObject()
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
        BasicDBObject obj = (BasicDBObject) repo.get("progress");

        if (!obj.getBoolean("stars_done")) {
            exec.submit(new UpdateStarsRunnable(stars, repos, stat, atom, fetcher, repo));
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
