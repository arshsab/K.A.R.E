package model;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import play.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author arshsab
 * @since 07 2014
 */

public class OrderRecommender  implements Recommender {
    private final Model model;
    private final DBCollection repos, scores;
    private ArrayList<Repo> allRepos = new ArrayList<>();
    private volatile Map<Integer, Integer> sortedPositions;

    public OrderRecommender(Model model) {
        this.model = model;
        repos = model.repos;
        scores = model.scores;

        ArrayList<Repo> all = new ArrayList<>();

        for (DBObject obj : repos.find()) {
            all.add(new Repo((BasicDBObject) obj));
        }

        addRepo(all);

        Logger.info("Recommender is setup...");
    }

    public void addRepo(final Repo r) {
        addRepo(new ArrayList<Repo>(){{
            add(r);
        }});
    }

    public synchronized void addRepo(ArrayList<Repo> repos) {
        Map<Integer, Integer> newSortedPositions = new ConcurrentHashMap<>();

        allRepos.addAll(repos);

        Collections.sort(allRepos, Collections.reverseOrder(new Comparator<Repo>() {
            @Override
            public int compare(Repo o1, Repo o2) {
                return o1.stars - o2.stars;
            }
        }));

        for (int i = 0; i < allRepos.size(); i++) {
            newSortedPositions.put(allRepos.get(i).rId, i);
        }

        sortedPositions = newSortedPositions;
    }

    @Override
    public Repo[] recommendationsFor(String repo, int size) {
        int rId = model.lookup(repo);

        ArrayList<BasicDBObject> canidates = new ArrayList<>();

        for (DBObject obj : scores.find(new BasicDBObject("a", rId))) {
            canidates.add((BasicDBObject) obj);
        }

        final Map<Integer, Integer> positions = this.sortedPositions;

        canidates.sort(Collections.reverseOrder(new Comparator<BasicDBObject>() {
            @Override
            public int compare(BasicDBObject o1, BasicDBObject o2) {
                return o1.getInt("s") - o2.getInt("s");
            }
        }));

        PriorityQueue<Reco> pq = new PriorityQueue<>(new Comparator<Reco>() {
            @Override
            public int compare(Reco o1, Reco o2) {
                return Double.compare(o1.score, o2.score);
            }
        });

        for (int i = 0; i < canidates.size(); i++) {
            int rId2 = canidates.get(i).getInt("b");

            if (rId == rId2)
                continue;

            int expectedPosition = positions.get(rId2);

            int numer = Math.abs(expectedPosition - i);
            int denom;

            if (expectedPosition > i) {
                denom = Math.max(expectedPosition, 1);
            } else {
                denom = positions.size() - expectedPosition;
            }

            double score = numer / (double) denom;

            pq.add(new Reco(score, rId2));

            if (pq.size() > size) {
                pq.poll();
            }
        }

        Repo[] repos = new Repo[pq.size()];

        for (int i = 0; i < repos.length; i++) {
            repos[repos.length - 1 - i] = model.getRepo(pq.poll().rId);
        }

        return repos;
    }

    private class Reco {
        final double score;
        final int rId;

        Reco(double score, int rId) {
            this.score = score;
            this.rId   = rId;
        }
    }
}
