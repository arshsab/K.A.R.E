package io.kare.server.recommend;

/**
 * @author arshsab
 * @since 03 2014
 */

public class Recommendation {
    public final String repoA, repoB, language, description;
    public final double score;
    public final int gazers;

    Recommendation(String repoA, String repoB, String language,
                   String description, int gazers, double score) {

        this.repoA = repoA;
        this.repoB = repoB;
        this.score = score;
        this.description = description;
        this.language = language;
        this.gazers = gazers;
    }


}
