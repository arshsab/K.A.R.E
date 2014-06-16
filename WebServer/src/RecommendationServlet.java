
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.mongodb.DB;
import com.mongodb.MongoClient;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.UnknownHostException;

public class RecommendationServlet extends HttpServlet {
    private  Recommender recommender;

    @Override
    public void init() throws ServletException {
        try {
            MongoClient client = new MongoClient();
            DB db = client.getDB("kare");
            recommender = new Recommender(db.getCollection("scores"), db.getCollection("repos"));

        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        super.init();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String owner = req.getParameter("owner"),
               name  = req.getParameter("repo");

        if (owner == null || name == null) {
            return;
        }

        System.out.println("Received request for:" + owner + "/" + name);

        String repo = owner + "/" +  name;

        repo = repo.toLowerCase();

        resp.setStatus(200);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        JsonGenerator jGen = new JsonFactory().createGenerator(resp.getOutputStream(), JsonEncoding.UTF8);

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
    }
}
