package io.kare.suggest.recovery;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import io.kare.suggest.tasks.Task;
import io.kare.suggest.tokens.Token;
import io.kare.suggest.tokens.UpdateTokenResult;
import io.kare.suggest.tokens.UpdateTokensTask;

import java.util.List;

/**
 * @author arshsab
 * @since 07 2014
 */

public class ReFeedDatabaseTask extends Task<UpdateTokenResult, UpdateTokenResult> {
    private final DBCollection stars, watchers;

    public ReFeedDatabaseTask(DBCollection stars, DBCollection watchers) {
        super(1, "Re-Feed Database");

        this.stars = stars;
        this.watchers = watchers;
    }

    @Override
    protected void consume(UpdateTokenResult result) {
        List<BasicDBObject> stars = result.tokens.get(Token.STARGAZERS);
        List<BasicDBObject> watchers = result.tokens.get(Token.WATCHERS);

        for (DBCursor cursor = this.stars.find(new BasicDBObject("name", result.repo)); cursor.hasNext(); ) {
            stars.add((BasicDBObject) cursor.next());
        }

        for (DBCursor cursor = this.watchers.find(new BasicDBObject("name", result.repo)); cursor.hasNext(); ) {
            watchers.add((BasicDBObject) cursor.next());
        }

        output(result);
    }
}
