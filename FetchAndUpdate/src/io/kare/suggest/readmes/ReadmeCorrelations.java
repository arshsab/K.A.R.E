package io.kare.suggest.readmes;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
        return Arrays.stream(words).filter(tags::contains).collect(Collectors.toList());
    }

    private static void initializeTags() {
        // todo: fix this path so it's the right location for tags.csv
        try {
            new BufferedReader(new FileReader("tags.csv")).lines().forEach(tags::add);
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
