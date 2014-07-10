package controllers;

import model.OrderRecommender;
import model.Model;
import model.Statistics;
import model.Repo;

import play.*;
import play.mvc.*;

import java.util.*;
import java.net.UnknownHostException;
import java.io.IOException;

import util.StringOutputStream;
import views.html.*;

import com.fasterxml.jackson.core.*;

public class Application extends Controller {
    private static final Model model = new Model();

    public static Result index() {
        return ok(index.render(model.stats));
    }

    public static Result auto() {
        String guess = request().getQueryString("term");

        if (guess == null) {
            return ok("[]");
        }

        guess = guess.toLowerCase();

        try {
            StringOutputStream out = new StringOutputStream();
            JsonGenerator jGen = new JsonFactory().createGenerator(out);

            jGen.writeStartArray();

            for (String s : model.auto.complete(guess)) {
                jGen.writeString(s);
            }

            jGen.writeEndArray();

            jGen.flush();
            jGen.close();

            return ok(out.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Result recommend(String owner, String repo) {
        repo = owner + '/' + repo;
        repo = repo.toLowerCase();

        if (model.getRepo(repo) == null) {
            return notFound();
        }

        return ok(results.render(model.getRepo(repo), convert(model.reco.recommendationsFor(repo, 20))));
    }

    private static ArrayList<Repo> convert(Repo[] repos) {
        ArrayList<Repo> ret = new ArrayList<>();

        Collections.addAll(ret, repos);

        return ret;
    }
}
