package io.kare.suggest.fetch;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import io.kare.suggest.Logger;
import io.kare.suggest.readmes.ReadmeCorrelations;
import io.kare.suggest.readmes.ReadmeOptionPair;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Adrian Chmielewski-Anders
 * @since 0.0.1
 */

public class ReadmeFetcher {
    // github has many ways of accessing the raw readmes. This is one
    // https://github.com/user/repo/raw/branch/file.extension
    // is another, but this one redirects ^^ to raw.github.com or
    // raw.githubusercontent.com... weird
    private static final String README_BASE_URL = "https://raw.github.com/";

    /**
     * list of all possible readmes because they are different repo to repo
     * i've tried to add them all in order of popularity, if there is a better
     * way around this let me know
     */
    private static final String[] POSSIBLE_READMES = {
            "README.md",
            "README.markdown",
            "README.txt",
            "README",
            "README.rst",
            "readme.md"
    };

    public static void fetch(DBCollection repos, DBCollection readmes) {

        ExecutorService exec = Executors.newFixedThreadPool(8);

        DBCursor repoCursor = repos.find();
        try {
            while (repoCursor.hasNext()) {
                exec.submit(() -> {
                    BasicDBObject repoObject = (BasicDBObject) repoCursor.next();
                    String url = getURL(repoObject);
                    // check if we've already indexed this readme, avoid having to try
                    // different possible readme options
                    DBCursor readmeCursor = readmes.find(new BasicDBObject("name",
                            repoObject.get("name")));
                    if (readmeCursor.size() > 0) {
                        BasicDBObject readmeObject = (BasicDBObject) readmeCursor.next();
                        String readme = easyGet(url + readmeObject.get("readme_name"));
                        readmeObject.append("readme", ReadmeCorrelations.getKeyWords(readme));
                        readmes.save(readmeObject);
                    } else {
                        ReadmeOptionPair pair = hardGet(url);
                        readmes.insert(new BasicDBObject("readme",
                                ReadmeCorrelations.getKeyWords(pair.getReadme()))
                                .append("readme_name", pair.getName())
                                .append("name", repoObject.get("name")));
                    }

                });
            }
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            repoCursor.close();
            exec.shutdown();
        }
    }

    private static ReadmeOptionPair hardGet(String url) {
        Http http = new Http();
        String readme;
        for (String possibility : POSSIBLE_READMES) {
            try {
                readme = http.get(url + possibility);
                if (!"Not Found".equals(readme)) {
                    return new ReadmeOptionPair(readme, possibility);
                }
            } catch (IOException e) {
                e.printStackTrace();
                Logger.warn("The possibility " + possibility +
                        " is not vaild for " + url);
            }
        }
        // default so we don't have to deal with null strings
        return new ReadmeOptionPair("", "");
    }

    private static String easyGet(String url) {
        try {
            return new Http().get(url);
        } catch (IOException e) {
            e.printStackTrace();
            Logger.fatal("IOException, was unable to easyGet url " + url);
            return "";
        }
    }

    private static String getURL(BasicDBObject object) {
        return README_BASE_URL + object.get("name") + object.get("branch")
                + "/";
    }
}