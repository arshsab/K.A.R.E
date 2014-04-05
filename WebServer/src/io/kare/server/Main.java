package io.kare.server;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import io.kare.server.recommend.Recommender;
import io.kare.server.servlets.LinearRecommendationsServlet;
import org.eclipse.jetty.rewrite.handler.RewriteHandler;
import org.eclipse.jetty.rewrite.handler.Rule;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

        handler.addServlet(LinearRecommendationsServlet.class, "/server/linear");

        HandlerList list = new HandlerList();

        RewriteHandler rewrite = new RewriteHandler();

        rewrite.addRule(new Rule() {
            private final List<String> staticFiles = new ArrayList<String>() {{
                add("/linear.html");
                add("/ajax.js");
                add("/linear.css");
            }};

            @Override
            public String matchAndApply(String target, HttpServletRequest request, HttpServletResponse response) throws IOException {
                if (!target.startsWith("/")) {
                    target = "/" + target;
                }

                if (target.startsWith("/server/")) {
                    return target;
                } else if (staticFiles.contains(target)) {
                    return target;
                } else {
                    return "/linear.html";
                }
            }
        });

        ResourceHandler resources = new ResourceHandler();
        resources.setResourceBase("web");
        resources.setWelcomeFiles(new String[] { "linear/recommend.html" });
        resources.setDirectoriesListed(true);

        list.addHandler(rewrite);
        list.addHandler(resources);
        list.addHandler(handler);

        server.setHandler(list);

        server.start();

        server.join();
    }
}
