package io.kare.suggest;

import com.mongodb.*;

import io.kare.suggest.fetch.Fetcher;
import io.kare.suggest.repos.RepoUpdateAlgorithm;
import io.kare.suggest.stars.CorrelationsAlgorithm;
import io.kare.suggest.stars.UpdateStarsAlgorithm;
import io.kare.suggest.statistic.IncrementedStatistic;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author arshsab
 * @since 03 2014
 */

public class Kare {
    private String version;
    private final Fetcher fetcher;
    private final DB data,
                     statistics;
    private final DBCollection stars,
                               repos,
                               scores,
                               runtime;

    Kare(DB data, DB statistics) {
        this.fetcher = new Fetcher();
        this.statistics = statistics;
        this.data = data;

        String[] collections = {
                "repos",
                "stars",
                "scores",
        };

        Arrays.stream(collections)
                .filter(s -> !data.collectionExists(s))
                .forEach(s -> data.createCollection(s, null));

        this.stars   = data.getCollection("stars");
        this.repos   = data.getCollection("repos");
        this.scores  = data.getCollection("scores");
        this.runtime = statistics.getCollection("runtime");

        this.version = System.getProperty("kare.version");
    }

    public void update() throws IOException {
        if (!setupRuntime()) {
            Logger.fatal("K.A.R.E is already running on the server.");
            return;
        }

        BasicDBObject currentTask = (BasicDBObject) runtime.findOne(new BasicDBObject("role", "current_task"));

        IncrementedStatistic repoStat        = new IncrementedStatistic("repos", statistics);
        IncrementedStatistic starStat        = new IncrementedStatistic("stars", statistics);
        IncrementedStatistic correlationStat = new IncrementedStatistic("correlations", statistics);

        if (currentTask.getString("value").equals("repo_updates")) {

            RepoUpdateAlgorithm.update(fetcher, repos, repoStat);

            currentTask.put("value", "star_updates");
            runtime.save(currentTask);
        }

        if (currentTask.getString("value").equals("star_updates")) {
            UpdateStarsAlgorithm starAlgo = new UpdateStarsAlgorithm(stars, repos, starStat, fetcher);

            for (DBObject obj : data.getCollection("repos").find()) {
                starAlgo.consume((BasicDBObject) obj);
            }

            starAlgo.completeProcessing();

            currentTask.put("value", "correlation_updates");
            runtime.save(currentTask);
        }

        if (currentTask.getString("value").equals("correlation_updates")) {
            CorrelationsAlgorithm.correlate(stars, repos, scores, correlationStat);

            currentTask.put("value", "done");
            runtime.save(currentTask);
        }


    }

    private boolean setupRuntime() {
        if (runtime.findOne(new BasicDBObject("role", "running")) == null) {
            runtime.insert(new BasicDBObject("role", "running"));

            BasicDBObject currentTask = (BasicDBObject) runtime.findOne(new BasicDBObject("role", "current_task"));

            if (currentTask == null || currentTask.get("value").equals("done")) {
                runtime.remove(new BasicDBObject("role", "current_task"));

                runtime.insert(new BasicDBObject()
                        .append("role", "current_task")
                        .append("value", "repo_updates")
                );
            }

            Runtime.getRuntime().addShutdownHook(
                    new Thread(() -> runtime.remove(new BasicDBObject("role", "running")))
            );

            return true;
        }

        return false;
    }
}
