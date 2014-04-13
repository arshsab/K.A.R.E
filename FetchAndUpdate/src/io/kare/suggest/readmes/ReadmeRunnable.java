package io.kare.suggest.readmes;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import io.kare.suggest.Logger;
import io.kare.suggest.fetch.Http;

import java.io.IOException;

/**
 * @author Adrian Chmielewski-Anders
 */

public class ReadmeRunnable implements Runnable {

    private BasicDBObject repoObject;
    private DBCollection readmes;

    /**
     * @see <a href="https://github.com/github/markup">github</a> for their
     * officially supported list of readme extensions. Since we are requesting an
     * actual file on the server, capitalization does matter
     */
    private final String[] POSSIBLE_READMES = {
            // all "markdown"
            "README.md",
            "Readme.md",
            "readme.md",
            "README.mdown",
            "Readme.mdown",
            "readme.mdown",
            "README.markdown",
            "Readme.markdown",
            "readme.markdown",
            // rst
            "README.rst",
            "Readme.rst",
            "readme.rst",
            // textile
            "README.textile",
            "Readme.textile",
            "readme.textile",
            // rdoc
            "README.rdoc",
            "Readme.rdoc",
            "readme.rdoc",
            // org
            "README.org",
            "Readme.org",
            "readme.org",
            // no extension
            "README",
            "Readme",
            "readme",
            // txt
            "README.txt",
            "Readme.txt",
            "readme.txt",
            // creole
            "README.creole",
            "Readme.creole",
            "readme.creole",
            // mediawiki
            "README.mediawiki",
            "Readme.mediawiki",
            "readme.mediawiki",
            //asciidoc, adoc, asc
            "README.asciidoc",
            "Readme.asciidoc",
            "readme.asciidoc",
            "README.adoc",
            "Readme.adoc",
            "readme.adoc",
            // pod
            "README.pod",
            "Readme.pod",
            "readme.pod"
    };
    // github has many ways of accessing the raw readmes. This is one
    // https://github.com/user/repo/raw/branch/file.extension
    // is another, but this one redirects ^^ to raw.github.com or
    // raw.githubusercontent.com... weird
    private static final String README_BASE_URL = "https://raw.github.com/";

    public ReadmeRunnable(BasicDBObject obj, DBCollection reamdes) {
        this.repoObject = obj;
        this.readmes = reamdes;
    }

    @Override
    public void run() {
        if (repoObject == null) return;
        String url = getURL(repoObject);
        // check if we've already indexed this readme, avoid having to try
        // different possible readme options
        DBCursor readmeCursor = readmes.find(new BasicDBObject("name",
                repoObject.get("name")));
        ReadmeCorrelations correlations = new ReadmeCorrelations();
        if (readmeCursor.size() > 0) {
            BasicDBObject readmeObject = (BasicDBObject) readmeCursor.next();
            if (!"".equals(readmeObject.get("readme_name"))) {
                String readme = easyGet(url + readmeObject.get("readme_name"));

                readmeObject.append("readme", correlations.getKeyWords(readme + repoObject.get("description")));
            } else {
                readmeObject.append("readme", correlations.getKeyWords((String) repoObject.get("description")));
            }
            readmes.save(readmeObject);
//            Logger.info("Done: " + url + readmeObject.get("readme_name"));
        } else {
            ReadmeOptionPair pair = hardGet(url);

            BasicDBObject obj =
                    new BasicDBObject("keywords",
                            correlations.getKeyWords(pair.getReadme() + repoObject.get("description")))
                            .append("readme_name", pair.getName())
                            .append("name", repoObject.get("name"));
            readmes.insert(obj);
//            Logger.info("Done: " + url + pair.getName());
        }
    }

    private ReadmeOptionPair hardGet(String url) {
        Http http = new Http();
        String readme;
        for (String possibility : POSSIBLE_READMES) {
            try {
                readme = http.get(url + possibility);
                if (!"Not Found".equals(readme)) {
                    return new ReadmeOptionPair(readme, possibility);
                }
            } catch (IOException e) {
//                e.printStackTrace();
            }
        }
        // default so we don't have to deal with null strings
        Logger.warn("Failed with all extensions for: " + url);
        return new ReadmeOptionPair("", "");
    }

    private String easyGet(String url) {
        try {
            return new Http().get(url);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    private String getURL(BasicDBObject object) {
        String branch = object.get("default_branch") == null ? "master" : (String) object.get("default_branch");
        return README_BASE_URL + object.get("name") + "/" +branch
                + "/";
    }
}
