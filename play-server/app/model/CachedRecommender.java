package model;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.sun.istack.internal.NotNull;

import java.util.List;

/**
 * @author Adrian
 */

public class CachedRecommender implements Recommender {
    private Model model;
    private DBCollection cachedRecs;

    public CachedRecommender(Model model) {
        this.model = model;
        this.cachedRecs = model.cachedRecs;
    }

    @Override
    @SuppressWarnings(value = {"unchecked"})
    public Repo[] recommendationsFor(String repo, int size) {
        // username/repo is guaranteed to be unique so we shouldn't get more than 1 result
        BasicDBObject recs = (BasicDBObject) cachedRecs.find(new BasicDBObject("repo", repo)).next();
        List<List<Object>> list = (List<List<Object>>) recs.get("recs");
        Repo[] ret = new Repo[list.size()];
        for (int i = 0; i < list.size(); i++) {
            ret[i] = new Repo((BasicDBObject) this.model.repos.find(
                    new BasicDBObject("name", repo)).next()
            );
        }
        return ret;
    }
}
