package io.kare.suggest;

import com.mongodb.*;

import io.kare.suggest.fetch.Fetcher;
import io.kare.suggest.recovery.OutOfDateRepoProducer;
import io.kare.suggest.recovery.ReFeedDatabaseTask;
import io.kare.suggest.repos.RepoUpdateTask;
import io.kare.suggest.tasks.Chain;
import io.kare.suggest.tasks.Producer;
import io.kare.suggest.tasks.Task;
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

        Chain chain;

        switch (System.getProperty("kare.run-type")) {
            case "update":
                chain = buildUpdateChain(fetcher, db, repos, stars, watchers, scores);
                break;
            case "recovery":
                chain = buildRecoveryChain(fetcher, db, repos, stars, watchers, scores);
                break;
            case "rebuild":
                chain = buildRedoScoresChain(fetcher, db, repos, stars, watchers, scores);
                break;
            default:
                throw new RuntimeException("Invalid run type: " + System.getProperty("kare.run-type"));
        }

        chain.start();
        chain.shutdown();
    }

    private Chain buildUpdateChain(Fetcher fetcher, DB db, DBCollection repos, DBCollection stars,
                                   DBCollection watchers, DBCollection scores) {

        return new Chain(new Task[] {
                new RepoUpdateTask(fetcher, repos),
                new UpdateTokensTask(db, fetcher),
                new TokenAnalysisTask(stars, watchers, repos, scores)
        });
    }

    private Chain buildRecoveryChain(Fetcher fetcher, DB db, DBCollection repos, DBCollection stars,
                                   DBCollection watchers, DBCollection scores) {

        return new Chain(new Task[] {
                new OutOfDateRepoProducer(repos),
                new UpdateTokensTask(db, fetcher),
                new TokenAnalysisTask(stars, watchers, repos, scores)
        });
    }

    private Chain buildRedoScoresChain(Fetcher fetcher, DB db, DBCollection repos, DBCollection stars,
                                       DBCollection watchers, DBCollection scores) {

        scores.drop();
        repos.update(new BasicDBObject(), new BasicDBObject("$set", new BasicDBObject("should_update", true)), false, true);

        DBCollection stars2 = db.getCollection("stars2");
        DBCollection watchers2 = db.getCollection("watchers2");

        return new Chain(new Task[] {
                new OutOfDateRepoProducer(repos),
                new UpdateTokensTask(db, fetcher),
                new ReFeedDatabaseTask(stars, watchers),
                new TokenAnalysisTask(stars2, watchers2, repos, scores),
                new Task<Void, Void>(0, 1, "Rename Collections") {
                    @Override protected void consume(Void o) {}

                    @Override public void shutdown() throws InterruptedException {
                        super.shutdown();

                        stars2.rename("stars", true);
                        watchers2.rename("watchers", true);
                    }
                }
        });
    }
}
