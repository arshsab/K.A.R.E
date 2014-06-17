import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

public class AutoCompleteServlet extends HttpServlet {
    private TreeSet<Repo> sortedByNames;

    @Override
    public void init() throws ServletException {
        super.init();

        DB kare = (DB) getServletContext().getAttribute("db");
        DBCollection repos = kare.getCollection("repos");

        this.sortedByNames = new TreeSet<>();

        for (DBObject obj : repos.find()) {
            BasicDBObject repo = (BasicDBObject) obj;

            String full = repo.getString("indexed_name");
            String partial = full.substring(full.indexOf("/") + 1);
            int stars = repo.getInt("gazers");

            this.sortedByNames.add(new Repo(partial, full, stars));
            this.sortedByNames.add(new Repo(full, full, stars));
        }

        System.out.println("AutoComplete Servlet set up.");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String query = req.getParameter("search");

        resp.setContentType("application/json");

        if (query == null || query.length() < 3) {
            resp.getWriter().write("[]");
            resp.getWriter().flush();
            resp.getWriter().close();
            return;
        }

        query = query.toLowerCase();

        PriorityQueue<Repo> shortList = new PriorityQueue<>(16, (one, two) -> one.score - two.score);

        for (Repo repo : this.sortedByNames.tailSet(new Repo(query, "", 0), true)) {
            if (!repo.name.startsWith(query)) {
                break;
            } else {
                shortList.add(repo);

                if (shortList.size() > 10) {
                    shortList.poll();
                }
            }
        }

        HashSet<String> sent = new HashSet<>(16);

        JsonGenerator jGen = new JsonFactory().createGenerator(resp.getOutputStream());

        jGen.writeStartObject();

        jGen.writeArrayFieldStart("results");

        ArrayList<Repo> finalList = new ArrayList<>();
        while (!shortList.isEmpty()) finalList.add(shortList.poll());

        for (int i = finalList.size() - 1; i >= 0; i--) {
            Repo repo = finalList.get(i);

            if (!sent.contains(repo.actual)) {
                sent.add(repo.actual);

                jGen.writeStartObject();

                jGen.writeStringField("name", repo.actual);

                jGen.writeEndObject();
            }
        }

        jGen.writeEndArray();

        jGen.writeEndObject();

        jGen.flush();
        jGen.close();

        resp.getOutputStream().flush();
        resp.getOutputStream().close();
    }

    private class Repo implements Comparable<Repo> {
        final String name, actual;
        final int score;

        Repo(String name, String actual, int score) {
            this.name = name;
            this.actual = actual;
            this.score = score;
        }

        @Override
        public int compareTo(Repo o) {
            return this.name.compareTo(o.name);
        }
    }
}

