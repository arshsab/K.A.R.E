package io.kare.suggest.readmes;

/**
 * A helper class
 *
 * @author Adrian Chmielewski-Anders
 * @since 0.0.1
 */

public class ReadmeOptionPair {
    private String readme, name;

    public ReadmeOptionPair(String readme, String name) {
        this.readme = readme;
        this.name = name;
    }

    public String getReadme() {
        return readme;
    }

    public String getName() {
        return name;
    }
}
