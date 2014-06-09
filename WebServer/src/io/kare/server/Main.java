package io.kare.server;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import io.kare.server.recommend.Recommender;
import io.kare.server.servlets.FileRoute;
import io.kare.server.servlets.LinearRecommendationsRoute;
import spark.Spark;

import java.io.File;

import static spark.Spark.*;

/**
 * @author arshsab
 * @since 03 2014
 */

public class Main {
    public static final String sep = File.pathSeparator;

    public static void main(String... args) throws Exception {
        if (args.length != 0)
            Spark.setPort(Integer.parseInt(args[0]));
        else
            Spark.setPort(80);

        staticFileLocation("web");

        get("/search/:repo/:owner", new FileRoute("web/results.html"));
        get("/index", new FileRoute("web/index.html"));

        get("/search_json", new LinearRecommendationsRoute());
    }
}
