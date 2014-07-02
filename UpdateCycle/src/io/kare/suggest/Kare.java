package io.kare.suggest;

import com.mongodb.*;

import io.kare.suggest.fetch.Fetcher;
import io.kare.suggest.recovery.OutOfDateRepoProducer;
import io.kare.suggest.repos.RepoUpdateTask;
import io.kare.suggest.tasks.Producer;
import io.kare.suggest.tokens.TokenAnalysisTask;
import io.kare.suggest.tokens.UpdateTokensTask;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author arshsab
 * @since 03 2014
 */

public class Kare {
    private final Fetcher fetcher;

    Kare() {
        this.fetcher = new Fetcher();
    }

    public void update(DB db) throws IOException, InterruptedException {
        String[] collections = {
                "repos",
                "stars",
                "watchers",
                "updates",
                "scores",
                "meta"
        };

        Arrays.stream(collections)
                .filter(s -> !db.collectionExists(s))
                .forEach(s -> db.createCollection(s, null));

        DBCollection repos = db.getCollection("repos");
        DBCollection stars = db.getCollection("stars");
        DBCollection watchers = db.getCollection("watchers");
        DBCollection scores = db.getCollection("scores");

        Producer<BasicDBObject> repoUpdates = Boolean.parseBoolean(System.getProperty("kare.recovery")) ?
                new OutOfDateRepoProducer(repos) :
                new RepoUpdateTask(fetcher, repos);

        UpdateTokensTask tokenUpdates = new UpdateTokensTask(db, repos, fetcher);
        TokenAnalysisTask tokenAnalysis = new TokenAnalysisTask(stars, watchers, repos, scores);

        repoUpdates.addConsumer(tokenUpdates);
        tokenUpdates.addConsumer(tokenAnalysis);

        repoUpdates.startChain();
        repoUpdates.shutdown();
    }
}
