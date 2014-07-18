package io.kare.suggest.tokens;

import com.mongodb.BasicDBObject;

import java.util.List;
import java.util.Map;

/**
 * @author arshsab
 * @since 07 2014
 */

public class UpdateTokenResult {
    public final Map<Token, List<BasicDBObject>> tokens;
    public final String repo;

    public UpdateTokenResult(String repo, Map<Token, List<BasicDBObject>> tokens) {
        this.repo = repo;
        this.tokens = tokens;
    }
}
