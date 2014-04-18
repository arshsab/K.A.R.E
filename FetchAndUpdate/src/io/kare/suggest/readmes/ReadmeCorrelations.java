package io.kare.suggest.readmes;

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


    public List<String> getKeyWords(String readme) {
        List<String> words = Arrays.stream(removeChars(readme)).collect(Collectors.toList());
        words = words.subList(0, Math.min(words.size() - 1, 401));
        synchronized (tags) {
            if (tags.isEmpty()) {
                initializeTags();
            }
        }
        return tags.stream().filter(words::contains).collect(Collectors.toList());
    }

    private void initializeTags() {
        try {
           for (String tag :tagConstants.tagWords.split("\n")) {
               tags.add(tag);
           }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private String[] removeChars(String readme) {
        // main regex, will remove all of the markdown specific characters
        // while preserving links, also removes a lot of the random chars like
        // ,.=-*<> etc.
        return readme.replaceAll("\n", "").replaceAll("[^A-Za-z0-9\\.&&[(.+) ?+\\[.+\\]]]|[#?+]|[`?"
                + "+]|[<|>|**|*|-|,|=|!]", " ").replaceAll("\\s+", "    ")
                .toLowerCase().trim().split(" ");
    }

    private double getRating(String readme1, String readme2) {
        double total = 0.0;
        double match = 0.0;
        ArrayList<String> tags1 = (ArrayList<String>) this.getKeyWords(readme1);
        ArrayList<String> tags2 = (ArrayList<String>) this.getKeyWords(readme2);
        total = tags1.size() + tags2.size();
        for (String fTag: tags1) {
            if (tags2.contains(fTag)) {
                match += 2;
            }
        }
        return match/total;
    }

}
