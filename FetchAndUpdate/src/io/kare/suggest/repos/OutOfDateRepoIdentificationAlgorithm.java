package io.kare.suggest.repos;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author arshsab
 * @since 03 2014
 */

public class OutOfDateRepoIdentificationAlgorithm {
    private final DB original, next;

    public OutOfDateRepoIdentificationAlgorithm(DB original, DB next) {
        this.original = original;
        this.next = next;
    }

    public Map<String, Integer> identify() {
        Map<String, Integer> ret = new HashMap<>();

        DBCollection originalRepos = original.getCollection("repos");
        DBCollection newRepos = next.getCollection("repos");

        DBCursor cursor = newRepos.find();
        while (cursor.hasNext()) {
            BasicDBObject newRepo = (BasicDBObject) cursor.next();
            BasicDBObject oldRepo = (BasicDBObject) originalRepos.findOne(
                    new BasicDBObject("name", newRepo.getString("name"))
            );

            if (oldRepo == null) {
                ret.put(newRepo.getString("name"), newRepo.getInt("gazers"));
            } else {
                int diff = newRepo.getInt("gazers") - oldRepo.getInt("gazers");

                ret.put(newRepo.getString("name"), diff);
            }
        }

        return ret;
    }
}
