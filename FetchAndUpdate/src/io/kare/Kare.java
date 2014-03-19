package io.kare;

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

    public void update() {
        updateRepoList();
        List<String> pendingUpdates = identifyOutDatedRepos();

        for (String repo : pendingUpdates) {
            updateStarsFor(repo);
        }
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
}
