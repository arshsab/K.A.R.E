package controllers;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObject;
import model.Model;
import model.Repo;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import util.StringOutputStream;
import views.html.index;
import views.html.results;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

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

    private static final Random rand = new Random();

    public static Result random() {
        int max = (int) model.repos.count();

        int num = rand.nextInt(max);

        String random = model.lookup(num);

        return redirect("/search/" + random);
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

    public static Result feedback() {
        Http.RequestBody data = request().body();

        try {
            JsonNode node = data.asJson();

            String a = node.path("a").textValue();
            String b = node.path("b").textValue();
            int score = node.path("score").asInt();

            BasicDBObject obj = new BasicDBObject()
                                    .append("a", a)
                                    .append("b", b)
                                    .append("score", score);

            model.feedback.insert(obj);

            return ok();
        } catch (Exception e) {
            e.printStackTrace();

            return badRequest();
        }
    }
}
