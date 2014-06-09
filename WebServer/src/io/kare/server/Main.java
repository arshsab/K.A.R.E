package io.kare.server;

import io.kare.server.servlets.FileRoute;
import io.kare.server.servlets.LinearRecommendationsRoute;
import spark.Spark;

import java.io.File;

import static spark.Spark.get;
import static spark.Spark.staticFileLocation;

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
            Spark.setPort(8081);

        staticFileLocation("/web");

        get("/search/:repo/:owner", new FileRoute("web/results.html"));
        get("/index", new FileRoute("web/index.html"));
        get("/", new FileRoute("web/index.html"));

        get("/search_json/:repo/:owner", new LinearRecommendationsRoute());
    }
}
