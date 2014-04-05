package io.kare.suggest;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.MongoClient;

import java.io.FileInputStream;
import java.io.IOException;

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

            Thread t = new Thread(() -> {
                while (Thread.interrupted()) {
                    try {
                        Thread.sleep(10000);
                    }  catch (InterruptedException e) {
                        break;
                    }

                    BasicDBObject obj = (BasicDBObject) db.getCollection("meta").findOne(new BasicDBObject("role", "refresh"));

                    if (obj.getBoolean("value")) {
                        obj.put("value", false);
                        try {
                            System.getProperties().load(new FileInputStream(args[0]));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    db.getCollection("meta").save(obj);
                }
            });

            t.setUncaughtExceptionHandler((t2, e) -> e.printStackTrace());

            t.start();

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

            t.interrupt();
            t.join();
        } catch (Throwable t) {
            t.printStackTrace();
        }

        System.out.flush();
        System.err.flush();
    }
}
