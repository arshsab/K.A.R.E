package io.kare.suggest.stars;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import io.kare.suggest.Logger;
import io.kare.suggest.fetch.Fetcher;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author arshsab
 * @since 03 2014
 */

public class UpdateStarsAlgorithm {
    public static void update(DBCollection updates, DBCollection stars, Fetcher fetcher) {
        Logger.important("Starting to update the repos stars.");

        stars.ensureIndex(new BasicDBObject()
            .append("repo", 1)
            .append("gazer", 1),
                new BasicDBObject()
            .append("unique", true)
            .append("dropDups", true)
        );

        ExecutorService exec = Executors.newFixedThreadPool(8);

        for (DBObject obj : updates.find(new BasicDBObject("new_stars", 1))) {
            BasicDBObject repo = (BasicDBObject) obj;

            String name  = repo.getString("repo");
            int newStars = repo.getInt("new_stars");

            exec.submit(new UpdateStarsRunnable(stars, fetcher, name, 0, (int) Math.ceil( newStars / 100.0)));
        }


        exec.shutdown();

        while (exec.isTerminated()) {
            try {
                exec.awaitTermination(1000, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        stars.ensureIndex(new BasicDBObject("repo", 1));
        stars.ensureIndex(new BasicDBObject("gazer", 1));

        Logger.important("Finished updating the repos stars.");
    }
}
