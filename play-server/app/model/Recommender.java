package model;

/**
 * @author arshsab
 * @since 07 2014
 */

public interface Recommender {
    public Repo[] recommendationsFor(String repo, int size);
}
