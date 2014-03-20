package io.kare;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.DB;
import com.mongodb.DBCollection;

import java.io.IOException;
import java.util.List;

/**
 * @author arshsab
 * @since 03 2014
 */

public class Kare {
    private final Fetcher fetcher;
    private final ObjectMapper mapper = new ObjectMapper();

    Kare() {
        this.fetcher = new Fetcher(System.getProperty("api-key"));
    }

    public void update(DB from,  DB to) throws IOException {
        new RepoUpdateAlgorithm(fetcher).update(to);

        List<String> pendingUpdates = identifyOutDatedRepos();

        for (String repo : pendingUpdates) {
            updateStarsFor(repo);
        }

        updateCorrelations();
    }

    private List<String> identifyOutDatedRepos() {
        // todo

        return null;
    }

    private void updateStarsFor(String repo) {
        // todo
    }

    private void updateCorrelations() {
        // todo
    }
}
