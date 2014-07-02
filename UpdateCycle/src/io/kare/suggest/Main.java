package io.kare.suggest;

import com.mongodb.*;

import java.io.FileInputStream;

/**
 * @author arshsab
 * @since 03 2014
 */

public class Main {
    public static void main(String... args) throws InterruptedException {
        Runnable shutdown = null;

        try {
            if (args.length == 0) {
                Logger.fatal("First parameter must be the name of a properties file with the configuration specs.");
                return;
            }

            System.getProperties().load(new FileInputStream(args[0]));

            MongoClient client = new MongoClient(System.getProperty("mongo.host"),
                    Integer.parseInt(System.getProperty("mongo.port")));

            final DB db = client.getDB(System.getProperty("mongo.db"));

            DBCollection runtime = db.getCollection("runtime");
            BasicDBObject obj = (BasicDBObject) runtime.findOne();

            if (obj == null) {
                runtime.insert(new BasicDBObject());
            } else {
                Logger.important("Another process is already running. Exiting...");
                return;
            }

            shutdown = () -> {
                Logger.important("Shutting down...");

                runtime.drop();
            };

            Runtime.getRuntime().addShutdownHook(new Thread(shutdown));


            Logger.important("Starting Kare. Version #" + System.getProperty("kare.version"));

            Kare kare = new Kare();

            Logger.important("Starting an update.");
            kare.update(db);
            Logger.important("Completed an update. Exiting...");
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            if (shutdown != null)
                shutdown.run();
        }
    }
}
