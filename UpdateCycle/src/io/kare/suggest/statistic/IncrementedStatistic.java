package io.kare.suggest.statistic;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author arshsab
 * @since 06 2014
 */

public class IncrementedStatistic {
    private final DB db;
    private final String name;
    private final DBCollection coll;
    private final AtomicInteger curr;

    public IncrementedStatistic(String name, DB db) {
        this.db = db;
        this.name = name;
        this.coll = db.getCollection(name);

        this.coll.ensureIndex(new BasicDBObject()
            .append("time", 1)
            .append("run", 1)
        );

        BasicDBObject obj = (BasicDBObject) this.coll
                .find()
                .sort(new BasicDBObject("time", -1))
                .limit(1)
                .next();

        int set;

        if (obj == null) {
            set = 0;
        } else {
            set = obj.getInt("value");
        }

        this.curr = new AtomicInteger(set);
    }

    public int increment() {
        int claim = curr.incrementAndGet();

        coll.insert(new BasicDBObject()
            .append("time", System.currentTimeMillis())
            .append("value", claim)
        );

        return claim;
    }

    public void archive() {

    }
}
