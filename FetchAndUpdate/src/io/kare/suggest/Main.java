package io.kare.suggest;

import com.mongodb.DB;
import com.mongodb.MongoClient;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author arshsab
 * @since 03 2014
 */

public class Main {
    public static void main(String... args) throws IOException, InterruptedException {
        if (args.length == 0) {
            Logger.fatal("First parameter must be the name of a properties file with the configuration specs.");
            return;
        }

        System.getProperties().load(new FileInputStream(args[0]));

        MongoClient client = new MongoClient(System.getProperty("mongo.host"),
                Integer.parseInt(System.getProperty("mongo.port")));

        Kare kare = new Kare();

        DB db = client.getDB(System.getProperty("mongo.db"));

        String _runs = System.getProperty("kare.runs");

        int runs;
        if (_runs == null) {
            runs = Integer.MAX_VALUE;
        } else {
            runs = Integer.parseInt(_runs);
        }

        kare.init(db);

        int i = 0;
        while (!Thread.interrupted() && i++ < runs) {
            Logger.important("Starting an update.");
            kare.update(db);
            Logger.important("Completed an update. Sleeping for 1 / 2 hour before next update.");
            Thread.sleep(1800 * 1000);
        }
    }
}
