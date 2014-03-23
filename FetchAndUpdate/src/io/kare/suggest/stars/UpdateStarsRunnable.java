package io.kare.suggest.stars;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import io.kare.suggest.Logger;
import io.kare.suggest.fetch.Fetcher;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author arshsab
 * @since 03 2014
 */

public class UpdateStarsRunnable implements Runnable {
    private static final ObjectMapper mapper = new ObjectMapper();

    private final DB to;
    private final String repo;
    private final int start, end;
    private final Fetcher fetcher;

    public UpdateStarsRunnable(DB to, Fetcher fetcher, String repo, int start, int end) {
        this.to = to;
        this.repo = repo;
        this.fetcher = fetcher;
        this.start = start;
        this.end = end;
    }

    @Override
    public void run() {
        DBCollection collection = to.getCollection("stars");

        for (int i = start; i <= end; i++) {
            String data;
            try {
                data = fetcher.fetch("repos/" + repo + "/stargazers?per_page=100&page=" + i);
            } catch (FileNotFoundException fnfe) {
                Logger.warn("Could not load in repo: " + repo);
                return;
            } catch (IOException e) {
                e.printStackTrace();
                Logger.fatal("Had a problem with getting a repo: " + e.getMessage());
                return;
            }

            JsonNode node;
            try {
                node = mapper.readTree(data);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            for (int j = 0; node.has(j); j++) {
                String name = node.path(j).get("login").textValue();

                collection.insert(new BasicDBObject()
                    .append("name", repo)
                    .append("gazer", name)
                );
            }
        }
    }
}
