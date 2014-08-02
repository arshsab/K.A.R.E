package model;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

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
    @SuppressWarnings("unchecked")
    public Repo[] recommendationsFor(String repo, int size) {
        repo = repo.toLowerCase();

        BasicDBObject recs = (BasicDBObject) cachedRecs.findOne(new BasicDBObject("repo", repo));
        BasicDBList list = (BasicDBList) recs.get("recs");
        Repo[] ret = new Repo[list.size()];
        for (int i = 0; i < list.size(); i++) {
            BasicDBList l = (BasicDBList) list.get(i);

            String rec = (String) l.get(1);

            ret[i] = model.getRepo(rec);
        }

        return ret;
    }
}
