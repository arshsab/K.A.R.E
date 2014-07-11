package io.kare.suggest;

import com.mongodb.*;

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

            System.getProperties().load(new FileInputStream(args[0]));

            MongoClient client = new MongoClient(System.getProperty("mongo.host"),
                    Integer.parseInt(System.getProperty("mongo.port")));

            final DB db = client.getDB(System.getProperty("mongo.db"));

            DBCollection runtime = db.getCollection("runtime");
            BasicDBObject obj = (BasicDBObject) runtime.findOne();

            if (obj == null) {
                runtime.insert(new BasicDBObject("val", 0).append("max", Integer.parseInt(System.getProperty("kare.parallelism"))));
            }

            obj = (BasicDBObject) runtime.findAndModify(new BasicDBObject(), new BasicDBObject("$inc", new BasicDBObject("val", 1)));

            int key = obj.getInt("val");
            int mod = obj.getInt("max");

            if (key >= mod) {
                Logger.fatal("Too many processes already running...");
                return;
            }

            Logger.info("Starting Kare Process #" + key);

            System.setProperty("kare.key", Integer.toString(key));
            System.setProperty("kare.mod", Integer.toString(mod));

            Logger.important("Starting Kare. Version #" + System.getProperty("kare.version"));

            Kare kare = new Kare();

            Logger.important("Starting an update.");
            kare.update(db);
            Logger.important("Completed an update. Exiting...");
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
