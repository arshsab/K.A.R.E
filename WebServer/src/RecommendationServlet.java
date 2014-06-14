
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.mongodb.DB;
import com.mongodb.MongoClient;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
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
        resp.setContentType("application/json; charset=utf-8");

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
        PrintWriter pw = resp.getWriter();
        pw.write(out.toString());
        pw.flush();
        pw.close();
    }
}
