package io.kare.suggest;

import com.mongodb.DB;
import com.mongodb.MongoClient;

import java.io.FileInputStream;

/**
 * @author arshsab
 * @since 03 2014
 */

public class Main {
    public static void main(String... args) throws InterruptedException {
        try {
            if (args.length == 0) {
                Logger.fatal("First parameter must be the name of a properties file with the configuration specs.");
                return;
            }

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down via shutdown hook.");
                System.out.flush();
                System.err.flush();
            }));

            System.getProperties().load(new FileInputStream(args[0]));

            Logger.important("Starting Kare. Version #0.0.1.");

            MongoClient client = new MongoClient(System.getProperty("mongo.host"),
                    Integer.parseInt(System.getProperty("mongo.port")));

            Kare kare = new Kare();

            DB db = client.getDB(System.getProperty("mongo.db"));

            Logger.important("Starting an update.");
            kare.update(db);
            Logger.important("Completed an update. Exiting...");

        } catch (Throwable t) {
            t.printStackTrace();
        }

        System.out.flush();
        System.err.flush();
    }
}
