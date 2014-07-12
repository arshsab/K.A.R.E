package model;

import com.mongodb.BasicDBObject;

/**
 * @author arshsab
 * @since 07 2014
 */

public class Repo {
    public final String language,
                        name,
                        indexedName,
                        description;
    public final int stars,
                     rId,
                     prominence;
    private final BasicDBObject mongoObject;

    public Repo(BasicDBObject mongoObject) {
        this.language = mongoObject.getString("language");
        this.name = mongoObject.getString("name");
        this.indexedName = mongoObject.getString("indexed_name");
        this.description = mongoObject.getString("description");

        this.stars = mongoObject.getInt("gazers");
        this.rId = mongoObject.getInt("r_id");
        this.prominence = mongoObject.getInt("prominence");

        this.mongoObject = mongoObject;
    }

    @Override
    public String toString() {
        return mongoObject.toString();
    }
}
