package io.kare;

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
        System.getProperties().load(new FileInputStream(args[0]));

        Thread.currentThread().setUncaughtExceptionHandler((t, e) -> e.printStackTrace());

        MongoClient client = new MongoClient(System.getProperty("mongo.host"),
                Integer.parseInt(System.getProperty("mongo.port")));

        Kare kare = new Kare();

        DB one = client.getDB("RecoA");
        DB two = client.getDB("RecoB");

        while (!Thread.interrupted()) {
            Logger.important("Starting an update.");
            kare.update(one, two);
            Logger.important("Completed an update. Sleeping for 1 / 2 hour before next update.");
            Thread.sleep(1800 * 1000);

            one.dropDatabase();

            DB temp = one;
            one = two;
            two = temp;
        }
    }
}
