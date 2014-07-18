package model;

import com.mongodb.DBCollection;

/**
 * @author arshsab
 * @since 07 2014
 */

public class Statistics {
    private final DBCollection repos, stars, watchers;

    public Statistics(Model model) {
        this.repos = model.repos;
        this.stars = model.stars;
        this.watchers = model.watchers;
    }

    public long getRepoSize() {
        return repos.count();
    }

    public long getStarSize() {
        return stars.count();
    }

    public long getWatcherSize() {
        return watchers.count();
    }
}
