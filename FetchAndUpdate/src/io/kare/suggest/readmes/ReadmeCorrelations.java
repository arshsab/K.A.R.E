package io.kare.suggest.readmes;

import io.kare.suggest.fetch.Http;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Adrian Chmielewski-Anders
 * @since 0.0.1
 */

public class ReadmeCorrelations {

    private static ArrayList<String> tags = new ArrayList<>();

    public static List<String> getKeyWords(String readme) {
        List<String> words = Arrays.stream(removeChars(readme)).collect(Collectors.toList());
        words = words.subList(0, Math.min(words.size(), 401));
        if (tags.isEmpty()) {
            initializeTags();
        }
        return tags.stream().filter(words::contains).collect(Collectors.toList());
    }

    private static void initializeTags() {
        // todo: fix this path so it's the right location for tags.txt
        try {
            Arrays.stream(new Http()
                    .get("https://raw.github.com/adrianc-a/kare/master/FetchReadme/Data/tags.txt")
                    .split("\n"))
                    .forEach(tags::add);
            System.out.println("printing tags" + tags);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static String[] removeChars(String readme) {
        // main regex, will remove all of the markdown specific characters
        // while preserving links, also removes a lot of the random chars like
        // ,.=-*<> etc.
        return readme.replaceAll("\n", "").replaceAll("[^A-Za-z0-9\\.&&[(.+) ?+\\[.+\\]]]|[#?+]|[`?"
                + "+]|[<|>|**|*|-|,|=|!]", " ").replaceAll("\\s+", "    ")
                .toLowerCase().trim().split(" ");
    }
}
