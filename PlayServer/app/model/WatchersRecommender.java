package model;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * @author arshsab
 * @since 07 2014
 */

public class WatchersRecommender implements Recommender {
    private final DBCollection watchers, scores;
    private final Model model;

    public WatchersRecommender(Model model) {
        this.watchers = model.watchers;
        this.scores = model.scores;

        this.model = model;
    }

    @Override
    public Repo[] recommendationsFor(String repo, int size) {
        int rId = model.lookup(repo);

        PriorityQueue<BasicDBObject> queue = new PriorityQueue<>(10, new Comparator<BasicDBObject>() {
            @Override
            public int compare(BasicDBObject o1, BasicDBObject o2) {
                return o1.getInt("w") - o2.getInt("w");
            }
        });

        for (DBObject obj : scores.find(new BasicDBObject("a", rId))) {
            BasicDBObject obj2 = (BasicDBObject) obj;

            queue.add(obj2);

            if (queue.size() > size) {
                queue.poll();
            }
        }

        Repo[] ret = new Repo[queue.size()];

        for (int i = 0; i < ret.length; i++) {
            ret[ret.length - i - 1] = model.getRepo(queue.poll().getInt("b"));
        }

        return ret;
    }
}
