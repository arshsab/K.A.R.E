package io.kare.server.servlets;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import io.kare.server.recommend.Recommendation;
import io.kare.server.recommend.Recommender;
import spark.Request;
import spark.Response;
import spark.Route;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.util.List;

/**
 * @author arshsab
 * @since 03 2014
 */

public class LinearRecommendationsRoute implements Route {
    private final Recommender recommender;

    {
        try {

            MongoClient client = new MongoClient();
            DB db = client.getDB("reco");
            recommender = new Recommender(db.getCollection("scores"), db.getCollection("repos"));

        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object handle(Request request, Response response) {
        try {
            if (request.params("owner") == null || request.params("repo") == null)
                return null;

            String repo = request.params(":owner") + request.params(":repo");

            repo = repo.toLowerCase();

            response.status(200);
            response.type("application/json");


            OutputStream out = new OutputStream() {
                final StringBuilder sb = new StringBuilder();

                @Override
                public void write(int b) throws IOException {
                    if (b != -1) {
                        sb.append((char) b);
                    }
                }

                @Override
                public String toString() {
                    return sb.toString();
                }
            };

            JsonGenerator jGen = new JsonFactory().createGenerator(out);

            jGen.writeStartArray();

            for (Recommendation reco : recommender.getLinearRecommendations(repo)) {
                jGen.writeStartObject();

                jGen.writeStringField("name", reco.repoB);
                jGen.writeStringField("language", reco.language);
                jGen.writeStringField("description", reco.description);
                jGen.writeNumberField("stars", reco.gazers);

                jGen.writeEndObject();
            }

            jGen.writeEndArray();

            jGen.flush();
            jGen.close();

            return out.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
