package io.kare.suggest;

import com.mongodb.BasicDBObject;

import java.io.IOException;

/**
 * @author arshsab
 * @since 04 2014
 */

@FunctionalInterface
public interface RepoConsumer {
    void consume(BasicDBObject repo) throws IOException;
    default void completeProcessing() { /* NO-OP */ }
}
