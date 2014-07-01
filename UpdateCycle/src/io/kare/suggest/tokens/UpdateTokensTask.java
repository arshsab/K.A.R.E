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
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author arshsab
 * @since 03 2014
 */

public class UpdateTokensTask extends Task<BasicDBObject, BasicDBObject> {
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

        try {
            for (Token token : tokens) {
                DBCollection stars = db.getCollection(token.dbName);

                // On most updates, we shouldn't get more than 1 page of stars.
                List<BasicDBObject> allStars = new ArrayList<>(100);

                outer:
                for (int i = 1; ; i++) {
                    List<BasicDBObject> newStars = new ArrayList<>(100);

                    String data;

                    HttpResponse resp = fetcher.fetch("repos/" + repo + "/" + token.urlSuffix + "?per_page=100&page=" + i);
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

                        newStars.add(new BasicDBObject()
                                .append("name", repo)
                                .append("gazer", name)
                        );
                    }


                    Logger.debug("Found " + newStars.size() + " new stars.");

                    allStars.addAll(newStars);

                    // Either we run out of stars
                    if (newStars.size() < 100) {
                        break;
                    }

                    // Or we start repeating ourselves
                    for (BasicDBObject star : newStars) {
                        if (stars.findOne(star) != null) {
                            break outer;
                        }
                    }
                }

                // Reverse the order in case we crash while in this loop.
                for (int i = allStars.size() - 1; i >= 0; i--) {
                    try {
                        stars.insert(allStars.get(i));
                        output(allStars.get(i));
                    } catch (MongoException.DuplicateKey ignored) {
                        Logger.debug("The DBCollection stars already has: " + allStars.get(i).getString("gazer")
                                + " starring " + repo);
                    }
                }

                obj.put("scraped_stars", (int) stars.count(new BasicDBObject("name", repo)));

                BasicDBObject progress = (BasicDBObject) obj.get("progress");
                progress.put("stars_done", true);

                repos.save(obj);

                BasicDBObject metric = (BasicDBObject) meta.findOne(new BasicDBObject("role", "stars_done"));
                metric.put("value", atom.incrementAndGet());
                meta.save(obj);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }

        Logger.info("Finished with updating repo: " + repo);
    }
}
