package io.kare.suggest.readmes;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Adrian Chmielewski-Anders
 * @since 0.0.1
 */

public class ReadmeCorrelations {

    private static ArrayList<String> tags = null;

    public static List<String> getKeyWords(String readme) {
        String[] words = removeChars(readme);

        if (tags == null) {
            initializeTags();
        }
        List<String> keywords = new ArrayList<>();
        Arrays.stream(words).forEach((String s) -> {
            if (tags.contains(s)) {
                keywords.add(s);
            }
        });
        return keywords;
    }

    private static void initializeTags() {
        // todo: fix this path so it's the right location for tags.csv
        try {
            new BufferedReader(new FileReader("tags.csv")).lines().forEach((s) -> {
                Collections.addAll(tags, s.split(", "));
            });
        } catch (FileNotFoundException ignored) {}
    }

    private static String[] removeChars(String readme) {
        // main regex, will remove all of the markdown specific characters
        // while preserving links, also removes a lot of the random chars like
        // ,.=-*<> etc.
        return readme.replaceAll("[^A-Za-z0-9\\.&&[(.+) ?+\\[.+\\]]]|[#?+]|[`?"
                + "+]|[<|>|**|*|-|,|=|!]", " ").replaceAll("\\s+", "")
                .toLowerCase().trim().split(" ");
    }
}
