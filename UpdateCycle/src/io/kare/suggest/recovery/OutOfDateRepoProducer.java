package io.kare.suggest.recovery;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import io.kare.suggest.tasks.Producer;

/**
 * @author arshsab
 * @since 07 2014
 */

public class OutOfDateRepoProducer extends Producer<BasicDBObject> {
    private final DBCollection repos;

    public OutOfDateRepoProducer(DBCollection repos) {
        super("Out of Date Repo Producer");

        this.repos = repos;
    }

    @Override
    protected void produce() {
        for (DBCursor cursor = repos.find(); cursor.hasNext(); ) {
            BasicDBObject repo = (BasicDBObject) cursor.next();

            if (repo.getBoolean("should_update")) {
                output(repo);
            }
        }
    }
}
