package io.kare.suggest.recovery;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import io.kare.suggest.tasks.Task;
import io.kare.suggest.tokens.UpdateTokenResult;
import io.kare.suggest.tokens.UpdateTokensTask;

/**
 * @author arshsab
 * @since 07 2014
 */

public class ReFeedDatabaseTask extends Task<UpdateTokenResult, UpdateTokenResult> {
    private final DBCollection stars, watchers;

    public ReFeedDatabaseTask(DBCollection stars, DBCollection watchers) {
        super(1, 10, "Re-Feed Database");

        this.stars = stars;
        this.watchers = watchers;
    }

    @Override
    protected void consume(UpdateTokenResult result) {
        // todo
    }
}
