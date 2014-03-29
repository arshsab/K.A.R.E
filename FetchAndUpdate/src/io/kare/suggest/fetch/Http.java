package io.kare.suggest.fetch;

import io.kare.suggest.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * @author arshsab
 * @since 03 2014
 */

public class Http {
    public String get(String url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

        try {
            Logger.info("In Http.get, getting data");
            String data = br.lines().map(s -> s + "\n")
                    .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                    .toString();
            Logger.info("Got Data!: " + data);

            return data;
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }
}