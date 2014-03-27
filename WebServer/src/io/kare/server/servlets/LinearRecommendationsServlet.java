package io.kare.server.servlets;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import io.kare.server.recommend.Recommendation;
import io.kare.server.recommend.Recommender;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author arshsab
 * @since 03 2014
 */

public class LinearRecommendationsServlet extends HttpServlet {
    private Recommender recommender;

    @Override
    public void init() {
        this.recommender = (Recommender) getServletContext().getAttribute("recommender");
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String repo = req.getParameter("repo");

        if (repo == null) return;

        resp.setContentType("application/json");
        JsonGenerator jGen = new JsonFactory().createGenerator(resp.getWriter());

        jGen.writeStartArray();

        for (Recommendation recommendation : recommender.getLinearRecommendations(repo)) {
            jGen.writeStartObject();
            jGen.writeNumberField("score", recommendation.getScore());
            jGen.writeStringField("RepoA", recommendation.getFirstRepo());
            jGen.writeStringField("RepoB", recommendation.getSecondRepo());
            jGen.writeEndObject();
        }

        jGen.writeEndArray();

        jGen.flush();
        jGen.close();
    }
}
