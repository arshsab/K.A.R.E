package io.kare.suggest.tokens;

/**
 * @author arshsab
 * @since 06 2014
 */

public enum Token {
    STARGAZERS("stargazers", "stars"),
    WATCHERS("subscribers", "watchers");

    final String urlSuffix, dbName;

    Token(String urlSuffix, String dbName) {
        this.urlSuffix = urlSuffix;
        this.dbName = dbName;
    }
}
