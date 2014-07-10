package model;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import play.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author arshsab
 * @since 07 2014
 */

public class AutoCompleter {
    private volatile ArrayList<String> sorted;
    private Map<String, Repo> lookups = new ConcurrentHashMap<>();

    public AutoCompleter(Model model) {
        sorted = new ArrayList<>();

        ArrayList<Repo> all = new ArrayList<>();

        for (DBObject obj : model.repos.find()) {
            all.add(new Repo((BasicDBObject) obj));
        }

        addRepo(all);

        Logger.info("AutoCompleter is setup...");
    }

    public void addRepo(final Repo r) {
        if (Collections.binarySearch(sorted, r.indexedName) < 0) {
            return;
        }

        addRepo(new ArrayList<Repo>(){{
            add(r);
        }});
    }

    public synchronized void addRepo(ArrayList<Repo> repos) {
        ArrayList<String> newSorted = new ArrayList<>(repos.size());

        Collections.copy(newSorted, sorted);


        for (Repo r : repos) {
            String[] potential = {
                    r.indexedName.substring(r.indexedName.indexOf("/") + 1),
                    r.indexedName
            };

            for (int i = 0; i < 2; i++) {
                if (lookups.containsKey(potential[i])
                        && lookups.get(potential[i]).stars > r.stars) {

                    continue;
                }

                lookups.put(potential[i], r);

                newSorted.add(potential[i]);
            }
        }

        Collections.sort(newSorted);

        sorted = newSorted;
    }

    public String[] complete(String attempt) {
        return complete(attempt, 5);
    }

    public String[] complete(String attempt, int num) {
        ArrayList<String> sorted = this.sorted;

        int index = Collections.binarySearch(sorted, attempt);

        index = index < 0 ? -index - 1 : index;

        Set<Repo> sent = new HashSet<>();

        PriorityQueue<Repo> heap = new PriorityQueue<>(num, new Comparator<Repo>() {
            @Override
            public int compare(Repo o1, Repo o2) {
                return o1.stars - o2.stars;
            }
        });

        for ( ; index < sorted.size() && sorted.get(index).startsWith(attempt); index++) {
            Repo r = lookups.get(sorted.get(index));

            if (sent.contains(r)) {
                continue;
            }

            heap.add(r);
            sent.add(r);

            if (heap.size() > num) {
                heap.poll();
            }
        }

        String[] ret = new String[heap.size()];

        for (int i = 0; i < ret.length; i++) {
            ret[ret.length - i - 1] = heap.poll().name;
        }

        return ret;
    }
}
