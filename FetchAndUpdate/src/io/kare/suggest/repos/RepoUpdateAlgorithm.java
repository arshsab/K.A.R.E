package io.kare.suggest.repos;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import io.kare.suggest.Logger;
import io.kare.suggest.fetch.Fetcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author arshsab
 * @since 03 2014
 */

public class RepoUpdateAlgorithm {
    private final ObjectMapper mapper = new ObjectMapper();
    private final Fetcher fetcher;

    public RepoUpdateAlgorithm(Fetcher fetcher) {
        this.fetcher = fetcher;
    }

    public void update(DB db) throws IOException {
        DBCollection repos = db.getCollection("repos");
        repos.createIndex(new BasicDBObject("name", 1), new BasicDBObject("unique", true));
        repos.createIndex(new BasicDBObject("stargazers", 1));

        int stars = Integer.parseInt(System.getProperty("star-threshold"));

        while (true) {
            List<JsonNode> newRepos = new ArrayList<>();

            for (int i = 1; i <= 10; i++) {
                String data = fetcher.fetch(
                        "/search/repositories?" +
                        "sort=stars" +
                        "&order=asc" +
                        "&per_page=100" +
                        "&page=" + i +
                        "&q=stars:>=" + stars
                );

                JsonNode node = mapper.readTree(data);

                node = node.path("items");

                node.forEach(newRepos::add);
            }

            for (JsonNode node : newRepos) {
                BasicDBObject repo = new BasicDBObject()
                        .append("name"          , node.path("full_name").textValue())
                        .append("owner"         , node.path("owner").path("login").textValue())
                        .append("description"   , node.path("description").textValue())
                        .append("gazers"        , node.path("stargazers_count").intValue())
                        .append("language"      , node.path("language").textValue())
                        .append("watchers"      , node.path("watchers_count").intValue())
                        .append("default_branch", node.path("default_branch").textValue())
                ;

                Logger.info("Inserting Repo: " + repo.toString());

                repos.insert(repo);
            }

            if (newRepos.size() < 1000) {
                break;
            }

            stars = newRepos.get(newRepos.size() - 1).get("stargazers_count").intValue();
        }
    }
}
