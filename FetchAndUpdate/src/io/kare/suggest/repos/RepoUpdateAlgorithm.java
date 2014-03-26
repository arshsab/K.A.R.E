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
import java.util.Objects;

/**
 * @author arshsab
 * @since 03 2014
 */

public class RepoUpdateAlgorithm {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Fetcher fetcher
            = new Fetcher(Objects.requireNonNull(System.getProperty("kare.fetch.api-key"), "No api-key."));

    public static int update(Fetcher fetcher, DBCollection repos, int since) throws IOException {
        Logger.important("Starting repo update from: " + since);

        repos.ensureIndex(new BasicDBObject("gazers", 1));

        int last = since;
        while (true) {
            JsonNode root = mapper.readTree(fetcher.fetch("/repositories?since=" + last));

            Logger.info("Grabbing repos from id #: " + last);

            for (JsonNode node : root) {
                BasicDBObject repo = new BasicDBObject()
                        .append("name", node.path("full_name").textValue())
                        .append("id"            , node.path("id").intValue())
                        .append("scraped_stars" , 0);

                Logger.info("Inserting Repo: " + repo.get("name"));

                repos.insert(repo);
                 last = node.path("id").intValue();
            }

            if (root.size() < 100) {
                break;
            }
        }

        Logger.important("Finished Repo Update.");
        return last;
    }
}
