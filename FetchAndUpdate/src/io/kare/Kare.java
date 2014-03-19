package io.kare;

import com.mongodb.DB;

import java.io.IOException;
import java.util.List;

/**
 * @author arshsab
 * @since 03 2014
 */

public class Kare {
    private final Fetcher fetcher;

    Kare() {
        this.fetcher = new Fetcher(System.getProperty("api-key"));
    }

    public void update(DB from,  DB to) throws IOException {
        updateRepoList();

        List<String> pendingUpdates = identifyOutDatedRepos();



        for (String repo : pendingUpdates) {
            updateStarsFor(repo);
        }

        updateCorrelations();
    }

    private void updateRepoList() {
        // todo
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
