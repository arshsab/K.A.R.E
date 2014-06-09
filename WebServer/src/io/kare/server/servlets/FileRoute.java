package io.kare.server.servlets;

import spark.Request;
import spark.Response;
import spark.Route;

import java.io.*;

/**
 * @author arshsab
 * @since 06 2014
 */

public class FileRoute implements Route {
    private final String text;

    public FileRoute(String path, String fileName) throws IOException {
        this.text = loadText(fileName);
    }

    public FileRoute(String fileName) throws IOException {
        this(fileName, fileName);
    }

    private String loadText(String fileName) throws IOException {
        FileInputStream stream = new FileInputStream(fileName);
        BufferedReader br = new BufferedReader(new InputStreamReader(stream));

        StringBuilder sb = new StringBuilder();

        br.lines()
          .map(s -> s + "\n")
          .forEach(sb::append);

        return sb.toString();
    }

    @Override
    public Object handle(Request request, Response response) {
        if (text == null) {
            response.status(404);
        } else {
            response.status(200);
            response.type("text/html");
        }

        return text;
    }
}
