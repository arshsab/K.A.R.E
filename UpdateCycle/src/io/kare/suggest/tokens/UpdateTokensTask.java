package io.kare.suggest.tokens;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoException;
import io.kare.suggest.Logger;
import io.kare.suggest.fetch.Fetcher;
import io.kare.suggest.fetch.HttpResponse;
import io.kare.suggest.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author arshsab
 * @since 03 2014
 */

public class UpdateTokensTask extends Task<BasicDBObject, UpdateTokenResult> {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Token[] tokens = { Token.WATCHERS, Token.STARGAZERS };

    private final AtomicInteger atom;
    private final DBCollection repos;
    private final DBCollection meta;
    private final Fetcher fetcher;
    private final DB db;

    public UpdateTokensTask(DB db, DBCollection repos, DBCollection meta, AtomicInteger atom, Fetcher fetcher) {
        super(8, "Star Updater");

        this.db = db;
        this.repos = repos;
        this.meta = meta;
        this.atom = atom;

        this.fetcher = fetcher;
    }

    @Override
    protected void consume(BasicDBObject obj) {
        String repo = obj.getString("indexed_name");

        Logger.info("Updating repo: " + repo);

        Map<Token, List<BasicDBObject>> tokenResult = new HashMap<>();

        try {
            for (Token tokenType : tokens) {
                DBCollection tokenCollection = db.getCollection(tokenType.dbName);

                // On most updates, we shouldn't get more than 1 page of stars.
                List<BasicDBObject> allTokens = new ArrayList<>(100);

                for (int i = 1; ; i++) {
                    List<BasicDBObject> newTokens = new ArrayList<>(100);

                    String data;

                    HttpResponse resp = fetcher.fetch("repos/" + repo + "/" + tokenType.urlSuffix + "?per_page=100&page=" + i);
                    if (resp.responseCode == 200) {
                        data = resp.response;
                    } else if (resp.responseCode == 404) {
                        Logger.warn("Could not load in repo: " + repo);
                        break;
                    } else if (resp.responseCode == 422) {
                        Logger.warn("Hit the limit # of stargazers with: " + repo);
                        break;
                    } else {
                        Logger.fatal("Had a problem with getting a repo: " + repo +
                                " received response code: " + resp.responseCode);
                        break;
                    }

                    JsonNode node;
                    try {
                        node = mapper.readTree(data);
                    } catch (IOException e) {
                        Logger.warn("Couldn't parse json: " + data);
                        throw new RuntimeException(e);
                    }

                    for (int j = 0; node.has(j); j++) {
                        String name = node.path(j).get("login").textValue();

                        newTokens.add(new BasicDBObject()
                                .append("name", repo)
                                .append("gazer", name)
                        );
                    }


                    Logger.debug("Found " + newTokens.size() + " new stars.");

                    boolean repeats = false;

                    // We start repeating ourselves
                    for (BasicDBObject token : newTokens) {
                        if (tokenCollection.findOne(token) != null) {
                            repeats = true;
                        } else {
                            allTokens.add(token);
                        }
                    }

                    // Or we run out of stars.
                    if (repeats || newTokens.size() < 100) {
                        break;
                    }
                }

                tokenResult.put(tokenType, allTokens);

                if (tokenType == Token.STARGAZERS)
                    obj.put("scraped_stars", (int) tokenCollection.count(new BasicDBObject("name", repo)));
            }

            BasicDBObject progress = (BasicDBObject) obj.get("progress");
            progress.put("stars_done", true);

            repos.save(obj);

            BasicDBObject metric = (BasicDBObject) meta.findOne(new BasicDBObject("role", "stars_done"));
            metric.put("value", atom.incrementAndGet());
            meta.save(obj);

            output(new UpdateTokenResult(repo, tokenResult));
        } catch (Throwable t) {
            t.printStackTrace();
        }


        Logger.info("Finished with updating repo: " + repo);
    }
}
