package io.kare.suggest.statistic;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author arshsab
 * @since 06 2014
 */

public class ClassicStatistic {
    private final DBCollection coll;
    private final String run;

    public ClassicStatistic(String name, String run, DB db) {
        this.coll = db.getCollection(name);
        this.run = run;

        this.coll.ensureIndex(new BasicDBObject()
                .append("time", 1)
                .append("run", 1)
        );
    }

    public void put(int value) {
        coll.insert(new BasicDBObject()
                .append("time", System.currentTimeMillis())
                .append("run", run)
                .append("value", value)
        );
    }
}