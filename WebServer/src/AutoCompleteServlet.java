import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;

public class AutoCompleteServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String query = req.getParameter("search");
        URL url;

        if (!query.contains("/")) {
            url = new URL("https://github.com/command_bar/users?q=" + query);
        } else {
            url = new URL("https://github.com/command_bar/repos_for/" + query.substring(0, query.indexOf("/") + 1));
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
        PrintWriter pw = resp.getWriter();
        StringBuilder buf = new StringBuilder();
        resp.setContentType("application/json");
        while (true) {
            String line = br.readLine();
            if (line == null) break;
            buf.append(line);
        }
        pw.write(buf.toString());
        pw.flush();
        pw.close();
    }
}
