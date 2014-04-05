package io.kare.suggest;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import io.kare.suggest.fetch.ReadmeFetcher;

import java.net.UnknownHostException;

/**
 * @author Adrian Chmielewski-Anders
 * @since 0.0.1
 */

public class TestFetchReadme {
    public static void main(String[] args) throws UnknownHostException {
        DB db = new MongoClient().getDB("reco");
        db.createCollection("readmes", null);

        // fetch the readmes and generate a list of keywords
        // for every repo based on the readme and description of
        // the readme
        Logger.info("Starting to fetch...");
        ReadmeFetcher.fetch(db.getCollection("repos"),
                db.getCollection("readmes"));

    }
}
