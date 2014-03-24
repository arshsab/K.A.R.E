package io.kare.suggest.fetch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * @author arshsab
 * @since 03 2014
 */

public class Http {
    public String get(String url) throws IOException {
        try {
            url = URLEncoder.encode(url, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

        String data = br.lines()
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();

        return data;
    }
}