package io.kare.server.recommend;

/**
 * @author arshsab
 * @since 03 2014
 */

public class Recommendation {
    final String repoA, repoB;
    final double score;

    Recommendation(String repoA, String repoB, double score) {
        this.repoA = repoA;
        this.repoB = repoB;
        this.score = score;
    }

    public String getFirstRepo() {
        return repoA;
    }

    public String getSecondRepo() {
        return repoB;
    }

    public double getScore() {
        return score;
    }
}
