
import com.mongodb.DB;
import com.mongodb.MongoClient;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class Main {
    public static void main(String[] args) throws Exception {
        Server server = new Server(args.length == 0 ? 8080 : Integer.parseInt(args[0]));

        ResourceHandler handler = new ResourceHandler();
        handler.setDirectoriesListed(true);
        handler.setResourceBase("web");
        handler.setWelcomeFiles(new String[]{"index.html"});

        MongoClient client = new MongoClient();
        DB db = client.getDB("kare");

        ServletContextHandler sh = new ServletContextHandler();
        sh.setAttribute("db", db);
        sh.setContextPath("/");

        ServletHolder auto = new ServletHolder(new AutoCompleteServlet());
        auto.setInitOrder(2);

        sh.addServlet(RecommendationServlet.class, "/searchjson");
        sh.addServlet(auto, "/auto");

        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{handler, sh});
        server.setHandler(handlers);

        server.start();
        server.join();
    }
}
