package io.kare.suggest.repos;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import io.kare.suggest.Logger;
import io.kare.suggest.fetch.Fetcher;
import io.kare.suggest.tasks.Producer;

import java.io.IOException;
import java.util.*;

/**
 * @author arshsab
 * @since 03 2014
 */

public class RepoUpdateTask extends Producer<BasicDBObject> {
    private final Map<Integer, Integer> lookupTable = new HashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();

    private final DBCollection repos;
    private final Fetcher fetcher;
    private final int threshold;
    private final int id;

    private final int UPPER_BOUND = 100_000;
    private final int LOWER_BOUND = 0;

    public RepoUpdateTask(Fetcher fetcher, DBCollection repos) {
        super("Repo Updates");
        this.fetcher = fetcher;
        this.repos = repos;
        this.threshold = Integer.parseInt(System.getProperty("kare.minimum-stars"));

        DBCursor curs = repos.find().sort(new BasicDBObject("r_id", -1));

        this.id = ((BasicDBObject) curs.next()).getInt("r_id");

    }

    public void update() throws IOException {
        Logger.important("Starting repo updates.");

        repos.ensureIndex(new BasicDBObject("indexed_name", 1));

        List<Integer> ranges = new ArrayList<>();

        int last = UPPER_BOUND;
        int n = 1000;
        while (true) {
            int nex = binSearch(n, fetcher);

            Logger.info("Found " + n + " repos above " + nex);

            if (nex == last) {
                break;
            }

            last = nex;

            ranges.add(nex);

            n += 1000;
        }

        int redos = 0;

        int upper = UPPER_BOUND;
        for (int lower : ranges) {
            if (upper < threshold) {
                break;
            }

            for (int i = 1; i <= 10; i++) {
                Logger.info("Grabbing repos from: " + upper + " to " + lower);

                JsonNode root = mapper.readTree(fetcher.fetch("/search/repositories?" +
                        "per_page=100" +
                        "&page=" + i +
                        "&q=stars:" + lower + ".." + upper
                ).response);

                root = root.path("items");

                for (JsonNode node : root) {
                    BasicDBObject repo = (BasicDBObject) repos.findOne(
                            new BasicDBObject("indexed_name", node.path("full_name").textValue().toLowerCase())
                    );

                    if (node.path("stargazers_count").intValue() < threshold) {
                        continue;
                    }

                    boolean newRepo = repo == null;

                    if (newRepo) {
                        repo = new BasicDBObject("scraped_stars", 0);
                        repo.put("r_id", id);
                    }

                    repo.put("indexed_name", node.path("full_name").textValue().toLowerCase());
                    repo.put("name", node.path("full_name").textValue());
                    repo.put("pic_url", node.path("owner").path("avatar_url").textValue());
                    repo.put("gazers", node.path("stargazers_count").intValue());
                    repo.put("watchers", node.path("watchers_count").intValue());
                    repo.put("description", node.path("description").textValue());
                    repo.put("default_branch", node.path("default_branch").textValue());
                    repo.put("language", node.path("language").textValue());
                    repo.put("owner", node.path("owner").path("login").textValue());

                    boolean shouldRedo  = repo.getInt("gazers") / ((double) Math.max(repo.getInt("scraped_stars"), 1)) > 1.075;


                    BasicDBObject progress = new BasicDBObject();
                    progress.put("stars_done", !shouldRedo);
                    progress.put("correlations_done", !shouldRedo);

                    repo.put("progress", progress);

                    Logger.info("Inserting Repo: " + repo.get("name"));

                    if (newRepo)
                        repos.insert(repo);
                    else
                        repos.save(repo);

                    if (shouldRedo) {
                        ++redos;
                        output(repo);
                    }
                }

                if (root.size() < 100) {
                    break;
                }
            }

            upper = lower;
        }
    }

    private int binSearch(int num, Fetcher fetcher) throws IOException {
        int low = LOWER_BOUND;
        int high = UPPER_BOUND;

        while (low < high) {
            int mid = low + (high - low) / 2;
            if (grab(mid, fetcher) < num) {
                high = mid;
            } else {
                low = mid + 1;
            }
        }

        if (grab(low, fetcher) > num) {
            return UPPER_BOUND;
        }

        return low;
    }

    private int grab(int num, Fetcher fetcher) throws IOException {
        if (lookupTable.containsKey(num)) {
            return lookupTable.get(num);
        }

        Logger.debug("Grabbing: " + num);

        String json = fetcher.fetch("/search/repositories?q=stars:" + num + ".." + UPPER_BOUND).response;

        JsonNode root = mapper.readTree(json);

        int count = root.path("total_count").intValue();

        lookupTable.put(num, count);

        return count;
    }

    @Override
    protected void produce() {
        try {
            update();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            throw new RuntimeException(ioe);
        }
    }
}