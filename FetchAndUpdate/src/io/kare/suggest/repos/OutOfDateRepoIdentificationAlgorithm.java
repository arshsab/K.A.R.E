package io.kare.suggest.repos;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import io.kare.suggest.Logger;
import io.kare.suggest.fetch.Fetcher;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author arshsab
 * @since 03 2014
 */

public class OutOfDateRepoIdentificationAlgorithm {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final int STAR_UPDATE_THRESHOLD;

    static {
        String prop = System.getProperty("suggest.repos.OutOfDateRepoIdentificationAlgorithm.update-threshold");

        STAR_UPDATE_THRESHOLD = prop == null ? 25 : Integer.parseInt(prop);
    }

    public static void identify(DBCollection repos, DBCollection updates, Fetcher fetcher) throws IOException {
        Logger.important("Starting to search for out-of-date repos. " + repos.count() + " repos to check.");

        DBCursor cursor = repos.find();
        while (cursor.hasNext()) {
            BasicDBObject repo = (BasicDBObject) cursor.next();
            String repoName = repo.getString("name");

            try {
                JsonNode root = mapper.readTree(fetcher.fetch("/repos/" + repoName));

                int alreadyScraped = repo.getInt("scraped_stars");
                int currentStarCount = root.path("stargazers_count").intValue();

                // Mark the repo as ready to be updates with new stars.
                if (currentStarCount - alreadyScraped >= STAR_UPDATE_THRESHOLD) {
                    updates.insert(new BasicDBObject()
                        .append("repo"     , repoName)
                        .append("new_stars", currentStarCount - alreadyScraped)
                    );
                }

                // Update all the repo logistics anyways.
                BasicDBObject repoUpdate = new BasicDBObject()
                    .append("$set", new BasicDBObject("gazers"        , root.path("stargazers_count")))
                    .append("$set", new BasicDBObject("watchers"      , root.path("watchers_count")))
                    .append("$set", new BasicDBObject("description"   , root.path("description")))
                    .append("$set", new BasicDBObject("default_branch", root.path("default_branch")))
                    .append("$set", new BasicDBObject("language"      , root.path("language")))
                    .append("$set", new BasicDBObject("owner"         , root.path("owner")));

                repos.update(new BasicDBObject("name", repoName), repoUpdate);
            } catch (FileNotFoundException fnfe) {
                Logger.warn("Could not locate: " + repo.getString("name"));
            }
        }

        Logger.important("Finised searching for out-of-date repos.");
    }
}
