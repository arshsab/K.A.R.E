
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;

public class Main {
    public static void main(String[] args) throws Exception {
        Server server = new Server(args.length == 0 ? 8080 : Integer.parseInt(args[0]));

        ResourceHandler handler = new ResourceHandler();
        handler.setDirectoriesListed(true);
        handler.setResourceBase("web");
        handler.setWelcomeFiles(new String[]{"index.html"});

        ServletContextHandler sh = new ServletContextHandler();
        sh.setContextPath("/");
        sh.addServlet(RecommendationServlet.class, "/searchjson");
        sh.addServlet(AutoCompleteServlet.class, "/auto");
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{handler, sh});
        server.setHandler(handlers);

        server.start();
        server.join();
    }
}
