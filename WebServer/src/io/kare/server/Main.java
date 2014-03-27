package io.kare.server;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import io.kare.server.recommend.Recommender;
import io.kare.server.servlets.LinearRecommendationsServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;

import java.net.UnknownHostException;

/**
 * @author arshsab
 * @since 03 2014
 */

public class Main {
    public static void main(String... args) throws Exception {
        Server server = new Server(Integer.parseInt(args[0]));

        MongoClient client = new MongoClient();

        DB db = client.getDB("reco");

        ServletContextHandler handler = new ServletContextHandler();
        handler.setAttribute("recommender", new Recommender(db.getCollection("scores"), db.getCollection("repos")));

        handler.addServlet(LinearRecommendationsServlet.class, "/linear");

        server.setHandler(handler);

        server.start();

        server.join();
    }
}
