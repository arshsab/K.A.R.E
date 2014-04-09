package io.kare.suggest.fetch;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import io.kare.suggest.readmes.ReadmeRunnable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Adrian Chmielewski-Anders
 * @since 0.0.1
 */

public class ReadmeFetcher {

    public void fetch(DBCollection repos, DBCollection readmes) {

        // max of 8 so github doesn't get too flooded with requests
        ExecutorService exec = Executors.newFixedThreadPool(8);
        DBCursor repoCursor = repos.find();
        try {
            while (repoCursor.hasNext()) {
                BasicDBObject repoObject = (BasicDBObject) repoCursor.next();
//                new ReadmeRunnable(repoObject, readmes).run();
                exec.submit(new ReadmeRunnable(repoObject, readmes));
            }
        } finally {
            repoCursor.close();
        }
        readmes.createIndex(new BasicDBObject("keywords", 1));
    }
}